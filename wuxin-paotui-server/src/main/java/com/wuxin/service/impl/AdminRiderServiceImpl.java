package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminRiderReasonDTO;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.enums.RiderAuditStatusEnum;
import com.wuxin.enums.RiderStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.service.AdminRiderService;
import com.wuxin.vo.AdminRiderOperationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminRiderServiceImpl implements AdminRiderService {

    private final RiderInfoMapper riderInfoMapper;

    public AdminRiderServiceImpl(RiderInfoMapper riderInfoMapper) {
        this.riderInfoMapper = riderInfoMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRiderOperationVO approve(Long riderId) {
        RiderInfoEntity rider = requireRider(riderId);
        if (!RiderAuditStatusEnum.PENDING.getCode().equals(rider.getAuditStatus())) {
            throw new BusinessException(ResultCode.RIDER_AUDIT_STATUS_ERROR);
        }
        return update(rider, RiderAuditStatusEnum.APPROVED, RiderStatusEnum.ENABLED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRiderOperationVO reject(Long riderId, AdminRiderReasonDTO request) {
        RiderInfoEntity rider = requireRider(riderId);
        if (!RiderAuditStatusEnum.PENDING.getCode().equals(rider.getAuditStatus())) {
            throw new BusinessException(ResultCode.RIDER_AUDIT_STATUS_ERROR);
        }
        return update(rider, RiderAuditStatusEnum.REJECTED, RiderStatusEnum.NOT_ENABLED, request.getReason().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRiderOperationVO enable(Long riderId) {
        RiderInfoEntity rider = requireRider(riderId);
        if (!RiderAuditStatusEnum.APPROVED.getCode().equals(rider.getAuditStatus())) {
            throw new BusinessException(ResultCode.RIDER_AUDIT_STATUS_ERROR);
        }
        if (!RiderStatusEnum.DISABLED.getCode().equals(rider.getRiderStatus())) {
            throw new BusinessException(ResultCode.RIDER_STATUS_ERROR);
        }
        return update(rider, RiderAuditStatusEnum.APPROVED, RiderStatusEnum.ENABLED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRiderOperationVO disable(Long riderId, AdminRiderReasonDTO request) {
        RiderInfoEntity rider = requireRider(riderId);
        if (!RiderAuditStatusEnum.APPROVED.getCode().equals(rider.getAuditStatus())) {
            throw new BusinessException(ResultCode.RIDER_AUDIT_STATUS_ERROR);
        }
        if (!RiderStatusEnum.ENABLED.getCode().equals(rider.getRiderStatus())) {
            throw new BusinessException(ResultCode.RIDER_STATUS_ERROR);
        }
        return update(rider, RiderAuditStatusEnum.APPROVED, RiderStatusEnum.DISABLED, request.getReason().trim());
    }

    private AdminRiderOperationVO update(
            RiderInfoEntity rider,
            RiderAuditStatusEnum auditStatus,
            RiderStatusEnum riderStatus,
            String reason) {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<RiderInfoEntity> update = new LambdaUpdateWrapper<>();
        update.eq(RiderInfoEntity::getId, rider.getId())
                .eq(RiderInfoEntity::getAuditStatus, rider.getAuditStatus())
                .eq(RiderInfoEntity::getRiderStatus, rider.getRiderStatus())
                .set(RiderInfoEntity::getAuditStatus, auditStatus.getCode())
                .set(RiderInfoEntity::getRiderStatus, riderStatus.getCode())
                .set(RiderInfoEntity::getRejectReason, reason)
                .set(RiderInfoEntity::getUpdateTime, now);
        if (riderInfoMapper.update(null, update) != 1) {
            throw new BusinessException(ResultCode.RIDER_STATUS_ERROR);
        }

        AdminRiderOperationVO result = new AdminRiderOperationVO();
        result.setRiderId(rider.getId());
        result.setAuditStatus(auditStatus.getCode());
        result.setAuditStatusText(auditStatus.getText());
        result.setRiderStatus(riderStatus.getCode());
        result.setRiderStatusText(riderStatus.getText());
        result.setRejectReason(reason);
        result.setOperationTime(now);
        return result;
    }

    private RiderInfoEntity requireRider(Long riderId) {
        if (riderId == null || riderId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        RiderInfoEntity rider = riderInfoMapper.selectById(riderId);
        if (rider == null) {
            throw new BusinessException(ResultCode.RIDER_NOT_EXIST);
        }
        return rider;
    }
}
