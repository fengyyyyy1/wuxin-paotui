package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.user.UpdateUserProfileDTO;
import com.wuxin.dto.wechat.BindWechatPhoneDTO;
import com.wuxin.dto.wechat.WeChatLoginDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.WeChatMiniProgramGateway;
import com.wuxin.gateway.WeChatMiniProgramGatewayRouter;
import com.wuxin.gateway.WeChatPhoneGateway;
import com.wuxin.gateway.WeChatPhoneGatewayRouter;
import com.wuxin.gateway.model.WeChatPhoneResult;
import com.wuxin.gateway.model.WeChatSessionResult;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.UserService;
import com.wuxin.utils.JwtUtils;
import com.wuxin.utils.PasswordUtils;
import com.wuxin.vo.UserInfoVO;
import com.wuxin.vo.WeChatLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private static final int USER_STATUS_ENABLED = 1;

    private static final int NOT_DELETED = 0;

    private static final int MAX_USERNAME_ATTEMPTS = 5;

    private static final int USERNAME_HASH_LENGTH = 32;

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private static final int PROFILE_NICKNAME_MAX_LENGTH = 30;

    private static final int PROFILE_AVATAR_MAX_LENGTH = 255;

    private static final int WECHAT_PHONE_CODE_MAX_LENGTH = 128;

    private static final Pattern MAINLAND_PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    private final WeChatMiniProgramGatewayRouter gatewayRouter;

    private final WeChatPhoneGatewayRouter phoneGatewayRouter;

    private final TransactionTemplate transactionTemplate;

    public UserServiceImpl(
            WeChatMiniProgramGatewayRouter gatewayRouter,
            WeChatPhoneGatewayRouter phoneGatewayRouter,
            TransactionTemplate transactionTemplate) {
        this.gatewayRouter = gatewayRouter;
        this.phoneGatewayRouter = phoneGatewayRouter;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public WeChatLoginVO wechatLogin(WeChatLoginDTO request) {
        if (request == null || isBlank(request.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        WeChatMiniProgramGateway gateway = gatewayRouter.getActiveGateway();
        WeChatSessionResult session = gateway.exchangeCode(request.getCode());
        validateSession(session);

        for (int attempt = 0; attempt < MAX_USERNAME_ATTEMPTS; attempt++) {
            int usernameAttempt = attempt;
            try {
                WeChatLoginVO result = transactionTemplate.execute(
                        status -> loginOrCreate(session, usernameAttempt));
                if (result != null) {
                    return result;
                }
            } catch (DuplicateKeyException exception) {
                WeChatLoginVO concurrentResult = transactionTemplate.execute(
                        status -> loginExistingAfterConflict(session));
                if (concurrentResult != null) {
                    return concurrentResult;
                }
            }
        }
        throw new BusinessException(ResultCode.WECHAT_USER_CREATE_FAILED);
    }

    @Override
    public UserInfoVO getProfile(Long userId) {
        return toUserInfoVO(getActiveUserById(userId));
    }

    @Override
    public void updateProfile(Long userId, UpdateUserProfileDTO request) {
        getActiveUserById(userId);
        validateProfileRequest(request);

        LambdaUpdateWrapper<UserEntity> update = new LambdaUpdateWrapper<>();
        update.eq(UserEntity::getId, userId)
                .eq(UserEntity::getIsDeleted, NOT_DELETED)
                .set(UserEntity::getNickname, request.getNickname())
                .set(UserEntity::getAvatar, request.getAvatar())
                .set(UserEntity::getGender, request.getGender())
                .set(UserEntity::getUpdateTime, LocalDateTime.now());
        if (baseMapper.update(null, update) == 0
                && findActiveUserById(userId) == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO bindWechatPhone(Long userId, BindWechatPhoneDTO request) {
        UserEntity user = getActiveUserById(userId);
        validateUserEnabled(user);

        String code = normalizePhoneAuthorizationCode(request);
        WeChatPhoneGateway gateway = phoneGatewayRouter.getActiveGateway();
        String phone = extractMainlandPhone(gateway.exchangeCode(code));

        if (baseMapper.countActiveUsersByPhoneExcludingUser(phone, userId) > 0) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_BOUND);
        }
        if (phone.equals(user.getPhone())) {
            return toUserInfoVO(user);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<UserEntity> update = new LambdaUpdateWrapper<>();
        update.eq(UserEntity::getId, userId)
                .eq(UserEntity::getIsDeleted, NOT_DELETED)
                .eq(UserEntity::getStatus, USER_STATUS_ENABLED)
                .set(UserEntity::getPhone, phone)
                .set(UserEntity::getUpdateTime, now);
        if (baseMapper.update(null, update) != 1) {
            UserEntity latestUser = findActiveUserById(userId);
            if (latestUser == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }
            validateUserEnabled(latestUser);
            if (phone.equals(latestUser.getPhone())) {
                return toUserInfoVO(latestUser);
            }
            throw new BusinessException(ResultCode.WECHAT_PHONE_SERVICE_ERROR);
        }

        user.setPhone(phone);
        user.setUpdateTime(now);
        log.info(
                "WeChat phone binding updated: userId={}, phone={}",
                userId,
                maskPhone(phone));
        return toUserInfoVO(user);
    }

    private void validateProfileRequest(UpdateUserProfileDTO request) {
        if (request == null
                || request.getGender() == null
                || request.getGender() < 0
                || request.getGender() > 2
                || (request.getNickname() != null
                    && request.getNickname().length() > PROFILE_NICKNAME_MAX_LENGTH)
                || (request.getAvatar() != null
                    && request.getAvatar().length() > PROFILE_AVATAR_MAX_LENGTH)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private String normalizePhoneAuthorizationCode(BindWechatPhoneDTO request) {
        if (request == null || isBlank(request.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        String code = request.getCode().trim();
        if (code.length() > WECHAT_PHONE_CODE_MAX_LENGTH) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        return code;
    }

    private String extractMainlandPhone(WeChatPhoneResult result) {
        if (result == null) {
            throw new BusinessException(ResultCode.WECHAT_PHONE_SERVICE_ERROR);
        }

        String phone = trimToNull(result.getPurePhoneNumber());
        if (phone == null) {
            phone = trimToNull(result.getPhoneNumber());
            String countryCode = trimToNull(result.getCountryCode());
            if (phone != null && ("86".equals(countryCode) || "+86".equals(countryCode))) {
                if (phone.startsWith("+86")) {
                    phone = phone.substring(3);
                } else if (phone.startsWith("86") && phone.length() == 13) {
                    phone = phone.substring(2);
                }
            }
        }

        if (phone == null || !MAINLAND_PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(ResultCode.PHONE_FORMAT_ERROR);
        }
        return phone;
    }

    private WeChatLoginVO loginOrCreate(WeChatSessionResult session, int usernameAttempt) {
        UserEntity existingUser = findActiveUserByOpenId(session.getOpenId());
        if (existingUser != null) {
            return loginExistingUser(existingUser, session.getUnionId());
        }

        UserEntity newUser = createWeChatUser(session, usernameAttempt);
        return buildLoginResult(newUser, true);
    }

    private WeChatLoginVO loginExistingAfterConflict(WeChatSessionResult session) {
        UserEntity existingUser = findActiveUserByOpenId(session.getOpenId());
        if (existingUser == null) {
            return null;
        }
        return loginExistingUser(existingUser, session.getUnionId());
    }

    private WeChatLoginVO loginExistingUser(UserEntity user, String unionId) {
        validateUserEnabled(user);
        if (!isBlank(unionId) && !unionId.equals(user.getUnionId())) {
            LambdaUpdateWrapper<UserEntity> update = new LambdaUpdateWrapper<>();
            update.eq(UserEntity::getId, user.getId())
                    .eq(UserEntity::getOpenId, user.getOpenId())
                    .eq(UserEntity::getIsDeleted, NOT_DELETED)
                    .set(UserEntity::getUnionId, unionId)
                    .set(UserEntity::getUpdateTime, LocalDateTime.now());
            if (baseMapper.update(null, update) != 1) {
                throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
            }
            user.setUnionId(unionId);
        }
        return buildLoginResult(user, false);
    }

    private UserEntity createWeChatUser(WeChatSessionResult session, int usernameAttempt) {
        String username = generateUsername(session.getOpenId(), usernameAttempt);
        if (baseMapper.countByUsernameIncludingDeleted(username) > 0) {
            throw new DuplicateKeyException("generated WeChat username already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        // A random BCrypt value prevents an auto-created WeChat account from using a fixed password.
        user.setPassword(PasswordUtils.encode(generateWeChatRandomPassword()));
        user.setOpenId(session.getOpenId());
        user.setUnionId(nullIfBlank(session.getUnionId()));
        user.setStatus(USER_STATUS_ENABLED);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsDeleted(NOT_DELETED);

        if (baseMapper.insert(user) != 1 || user.getId() == null) {
            throw new BusinessException(ResultCode.WECHAT_USER_CREATE_FAILED);
        }
        return user;
    }

    private UserEntity findActiveUserByOpenId(String openId) {
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.eq(UserEntity::getOpenId, openId)
                .eq(UserEntity::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1");
        return baseMapper.selectOne(query);
    }

    private UserEntity getActiveUserById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserEntity user = findActiveUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return user;
    }

    private UserEntity findActiveUserById(Long userId) {
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.eq(UserEntity::getId, userId)
                .eq(UserEntity::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1");
        return baseMapper.selectOne(query);
    }

    private WeChatLoginVO buildLoginResult(UserEntity user, boolean newUser) {
        validateUserEnabled(user);

        WeChatLoginVO result = new WeChatLoginVO();
        result.setToken(JwtUtils.generateToken(user.getId(), user.getUsername()));
        result.setUserInfo(toUserInfoVO(user));
        result.setNewUser(newUser);
        return result;
    }

    private UserInfoVO toUserInfoVO(UserEntity user) {
        UserInfoVO result = new UserInfoVO();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setAvatar(user.getAvatar());
        result.setPhone(user.getPhone());
        result.setGender(user.getGender());
        return result;
    }

    private void validateSession(WeChatSessionResult session) {
        if (session == null || isBlank(session.getOpenId())) {
            throw new BusinessException(ResultCode.WECHAT_OPENID_MISSING);
        }
        if (session.getOpenId().length() > 64) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }
    }

    private void validateUserEnabled(UserEntity user) {
        if (!Integer.valueOf(USER_STATUS_ENABLED).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.WECHAT_ACCOUNT_DISABLED);
        }
    }

    private String generateUsername(String openId, int attempt) {
        String digest = sha256(openId).substring(0, USERNAME_HASH_LENGTH);
        if (attempt == 0) {
            return "wx_" + digest;
        }
        return "wx_" + digest.substring(0, 23) + "_" + compactUuid().substring(0, 8);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    String generateWeChatRandomPassword() {
        String randomPassword = UUID.randomUUID().toString();
        if (randomPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new BusinessException(ResultCode.WECHAT_USER_CREATE_FAILED);
        }
        return randomPassword;
    }

    private String nullIfBlank(String value) {
        return isBlank(value) ? null : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
