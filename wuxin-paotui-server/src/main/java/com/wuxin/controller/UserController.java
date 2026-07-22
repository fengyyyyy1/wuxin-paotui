package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.UserLoginDTO;
import com.wuxin.dto.UserRegisterDTO;
import com.wuxin.dto.user.UpdateUserProfileDTO;
import com.wuxin.dto.wechat.BindWechatPhoneDTO;
import com.wuxin.dto.wechat.WeChatLoginDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.service.UserService;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.service.AdminPermissionService;
import com.wuxin.utils.JwtUtils;
import com.wuxin.utils.PasswordUtils;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.LoginVO;
import com.wuxin.vo.UserInfoVO;
import com.wuxin.vo.WeChatLoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class    UserController {

    private final UserService userService;
    private final AdminPermissionService adminPermissionService;
    private final AdminAuditLogService adminAuditLogService;

    public UserController(
            UserService userService,
            AdminPermissionService adminPermissionService,
            AdminAuditLogService adminAuditLogService) {
        this.userService = userService;
        this.adminPermissionService = adminPermissionService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/list")
    public Result<List<UserEntity>> list() {
        return Result.success(userService.list());
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        boolean exists = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userRegisterDTO.getUsername())
                .exists();
        if (exists) {
            return Result.fail(ResultCode.USERNAME_EXIST);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRegisterDTO.getUsername());
        userEntity.setPassword(PasswordUtils.encode(userRegisterDTO.getPassword()));
        userEntity.setPhone(userRegisterDTO.getPhone());
        userService.save(userEntity);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request) {
        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userLoginDTO.getUsername())
                .one();
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }
        if (!PasswordUtils.matches(userLoginDTO.getPassword(), user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR);
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            return Result.fail(ResultCode.WECHAT_ACCOUNT_DISABLED);
        }

        user.setLastLoginTime(java.time.LocalDateTime.now());
        user.setLastLoginIp(resolveIp(request));
        userService.updateById(user);

        UserInfoVO userInfoVO = buildUserInfoVO(user);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(JwtUtils.generateToken(user.getId(), user.getUsername()));
        loginVO.setUserInfo(userInfoVO);
        if (adminPermissionService.isAdmin(user.getId())) {
            adminAuditLogService.recordForUser(
                    user.getId(), "auth", "admin:login", "管理员登录",
                    "USER", user.getId(), null, java.util.Map.of("username", user.getUsername()));
        }
        return Result.success(loginVO);
    }

    @PostMapping("/wechat/login")
    public Result<WeChatLoginVO> wechatLogin(
            @Valid @RequestBody WeChatLoginDTO weChatLoginDTO) {
        return Result.success("微信登录成功", userService.wechatLogin(weChatLoginDTO));
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(buildUserInfoVO(user));
    }

    @GetMapping("/profile")
    public Result<UserInfoVO> getProfile() {
        return Result.success(userService.getProfile(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Valid @RequestBody UpdateUserProfileDTO updateUserProfileDTO) {
        userService.updateProfile(UserContext.getUserId(), updateUserProfileDTO);
        return Result.success();
    }

    @PostMapping("/phone/bind")
    public Result<UserInfoVO> bindWechatPhone(
            @Valid @RequestBody BindWechatPhoneDTO bindWechatPhoneDTO) {
        return Result.success(
                "手机号绑定成功",
                userService.bindWechatPhone(
                        UserContext.getUserId(),
                        bindWechatPhoneDTO));
    }

    private UserInfoVO buildUserInfoVO(UserEntity user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setGender(user.getGender());
        return userInfoVO;
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
