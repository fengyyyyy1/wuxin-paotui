package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.rider.RiderApplyDTO;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.RiderAuditStatusEnum;
import com.wuxin.enums.RiderStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.RiderProfileService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.RiderApplyVO;
import com.wuxin.vo.RiderProfileVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RiderProfileServiceImpl implements RiderProfileService {

    private final RiderInfoMapper riderInfoMapper;
    private final UserMapper userMapper;

    public RiderProfileServiceImpl(RiderInfoMapper riderInfoMapper, UserMapper userMapper) {
        this.riderInfoMapper = riderInfoMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiderApplyVO apply(RiderApplyDTO request) {
        Long userId = currentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请先绑定手机号");
        }

        RiderInfoEntity existing = findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            if (!RiderAuditStatusEnum.REJECTED.getCode().equals(existing.getAuditStatus())) {
                throw new BusinessException(ResultCode.RIDER_ALREADY_APPLIED);
            }
            LambdaUpdateWrapper<RiderInfoEntity> update = new LambdaUpdateWrapper<>();
            update.eq(RiderInfoEntity::getId, existing.getId())
                    .eq(RiderInfoEntity::getUserId, userId)
                    .eq(RiderInfoEntity::getAuditStatus, RiderAuditStatusEnum.REJECTED.getCode())
                    .set(RiderInfoEntity::getRealName, request.getRealName().trim())
                    .set(RiderInfoEntity::getIdCard, request.getIdCard().trim().toUpperCase())
                    .set(RiderInfoEntity::getIdCardFront, request.getIdCardFront().trim())
                    .set(RiderInfoEntity::getIdCardBack, request.getIdCardBack().trim())
                    .set(RiderInfoEntity::getAuditStatus, RiderAuditStatusEnum.PENDING.getCode())
                    .set(RiderInfoEntity::getRiderStatus, RiderStatusEnum.NOT_ENABLED.getCode())
                    .set(RiderInfoEntity::getRejectReason, null)
                    .set(RiderInfoEntity::getUpdateTime, now);
            if (riderInfoMapper.update(null, update) != 1) {
                throw new BusinessException(ResultCode.RIDER_AUDIT_STATUS_ERROR);
            }
            return toApplyVO(existing.getId(), now);
        }

        RiderInfoEntity rider = new RiderInfoEntity();
        rider.setUserId(userId);
        rider.setRealName(request.getRealName().trim());
        rider.setIdCard(request.getIdCard().trim().toUpperCase());
        rider.setIdCardFront(request.getIdCardFront().trim());
        rider.setIdCardBack(request.getIdCardBack().trim());
        rider.setAuditStatus(RiderAuditStatusEnum.PENDING.getCode());
        rider.setRiderStatus(RiderStatusEnum.NOT_ENABLED.getCode());
        rider.setRejectReason(null);
        rider.setCreateTime(now);
        rider.setUpdateTime(now);
        try {
            if (riderInfoMapper.insert(rider) != 1 || rider.getId() == null) {
                throw new IllegalStateException("rider application save failed");
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.RIDER_ALREADY_APPLIED);
        }
        return toApplyVO(rider.getId(), now);
    }

    @Override
    public RiderProfileVO getCurrentProfile() {
        Long userId = currentUserId();
        RiderInfoEntity rider = findByUserId(userId);
        if (rider == null) {
            throw new BusinessException(ResultCode.RIDER_NOT_EXIST);
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        RiderProfileVO result = new RiderProfileVO();
        result.setRiderId(rider.getId());
        result.setUserId(userId);
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setAvatar(user.getAvatar());
        result.setPhone(user.getPhone());
        result.setRealName(rider.getRealName());
        result.setIdCardMasked(maskIdCard(rider.getIdCard()));
        result.setIdCardFront(rider.getIdCardFront());
        result.setIdCardBack(rider.getIdCardBack());
        result.setAuditStatus(rider.getAuditStatus());
        result.setAuditStatusText(RiderAuditStatusEnum.getTextByCode(rider.getAuditStatus()));
        result.setRiderStatus(rider.getRiderStatus());
        result.setRiderStatusText(RiderStatusEnum.getTextByCode(rider.getRiderStatus()));
        result.setRejectReason(rider.getRejectReason());
        result.setApplyTime(rider.getCreateTime());
        result.setUpdateTime(rider.getUpdateTime());
        return result;
    }

    private RiderInfoEntity findByUserId(Long userId) {
        LambdaQueryWrapper<RiderInfoEntity> query = new LambdaQueryWrapper<>();
        query.eq(RiderInfoEntity::getUserId, userId).last("LIMIT 1");
        return riderInfoMapper.selectOne(query);
    }

    private RiderApplyVO toApplyVO(Long riderId, LocalDateTime applyTime) {
        RiderApplyVO result = new RiderApplyVO();
        result.setRiderId(riderId);
        result.setAuditStatus(RiderAuditStatusEnum.PENDING.getCode());
        result.setAuditStatusText(RiderAuditStatusEnum.PENDING.getText());
        result.setApplyTime(applyTime);
        return result;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return "***";
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
