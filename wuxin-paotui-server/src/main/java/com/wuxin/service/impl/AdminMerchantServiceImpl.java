package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminMerchantPageQueryDTO;
import com.wuxin.dto.admin.AdminMerchantReasonDTO;
import com.wuxin.dto.admin.ApproveMerchantDTO;
import com.wuxin.entity.MerchantAuditLogEntity;
import com.wuxin.entity.MerchantInfoEntity;
import com.wuxin.entity.MerchantStoreEntity;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.enums.MerchantAuditActionEnum;
import com.wuxin.enums.MerchantAuditStatusEnum;
import com.wuxin.enums.MerchantStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.AdminMerchantMapper;
import com.wuxin.mapper.MerchantAuditLogMapper;
import com.wuxin.mapper.MerchantInfoMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.service.AdminMerchantService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.AdminMerchantDetailVO;
import com.wuxin.vo.AdminMerchantOperationVO;
import com.wuxin.vo.AdminMerchantPageVO;
import com.wuxin.vo.PageResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private static final int NOT_DELETED = 0;

    private static final int REASON_MIN_LENGTH = 2;

    private static final int TEXT_MAX_LENGTH = 255;

    private final AdminMerchantMapper adminMerchantMapper;

    private final MerchantInfoMapper merchantInfoMapper;

    private final MerchantStoreMapper merchantStoreMapper;

    private final MerchantAuditLogMapper merchantAuditLogMapper;

    public AdminMerchantServiceImpl(
            AdminMerchantMapper adminMerchantMapper,
            MerchantInfoMapper merchantInfoMapper,
            MerchantStoreMapper merchantStoreMapper,
            MerchantAuditLogMapper merchantAuditLogMapper) {
        this.adminMerchantMapper = adminMerchantMapper;
        this.merchantInfoMapper = merchantInfoMapper;
        this.merchantStoreMapper = merchantStoreMapper;
        this.merchantAuditLogMapper = merchantAuditLogMapper;
    }

    @Override
    public PageResultVO<AdminMerchantPageVO> page(
            AdminMerchantPageQueryDTO query) {
        AdminMerchantPageQueryDTO safeQuery = validatePageQuery(query);
        Page<AdminMerchantPageVO> page = adminMerchantMapper.selectMerchantPage(
                new Page<>(safeQuery.getPageNum(), safeQuery.getPageSize()),
                safeQuery.getAuditStatus(),
                safeQuery.getMerchantStatus(),
                safeQuery.getKeyword());
        page.getRecords().forEach(this::enrichStatusText);
        return toPageResult(page);
    }

    @Override
    public AdminMerchantDetailVO detail(Long merchantId) {
        validateMerchantId(merchantId);
        AdminMerchantDetailVO detail =
                adminMerchantMapper.selectMerchantDetail(merchantId);
        if (detail == null) {
            throw new BusinessException(ResultCode.ADMIN_MERCHANT_NOT_EXIST);
        }
        enrichStatusText(detail);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMerchantOperationVO approve(
            Long merchantId,
            ApproveMerchantDTO request) {
        validateMerchantId(merchantId);
        String auditRemark = validateAuditRemark(request);
        Long adminUserId = getCurrentUserId();
        MerchantInfoEntity current = getMerchant(merchantId);
        if (!MerchantAuditStatusEnum.PENDING.getCode()
                .equals(current.getAuditStatus())) {
            throw new BusinessException(
                    ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantInfoEntity> update =
                baseMerchantUpdate(merchantId);
        update.eq(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.PENDING.getCode())
                .set(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.APPROVED.getCode())
                .set(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.ENABLED.getCode())
                .set(MerchantInfoEntity::getAuditAdminId, adminUserId)
                .set(MerchantInfoEntity::getAuditTime, now)
                .set(MerchantInfoEntity::getAuditRemark, auditRemark)
                .set(MerchantInfoEntity::getRejectReason, null)
                .set(MerchantInfoEntity::getUpdateTime, now);
        if (merchantInfoMapper.update(null, update) != 1) {
            handleAuditFailure(merchantId);
        }

        updateStoreState(
                merchantId,
                MerchantStatusEnum.ENABLED.getCode(),
                null,
                now);
        insertAuditLog(
                merchantId,
                adminUserId,
                MerchantAuditActionEnum.APPROVE,
                current.getAuditStatus(),
                MerchantAuditStatusEnum.APPROVED.getCode(),
                auditRemark,
                now);
        return buildOperationVO(merchantId, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMerchantOperationVO reject(
            Long merchantId,
            AdminMerchantReasonDTO request) {
        validateMerchantId(merchantId);
        String reason = validateReason(request);
        Long adminUserId = getCurrentUserId();
        MerchantInfoEntity current = getMerchant(merchantId);
        if (!MerchantAuditStatusEnum.PENDING.getCode()
                .equals(current.getAuditStatus())) {
            throw new BusinessException(
                    ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantInfoEntity> update =
                baseMerchantUpdate(merchantId);
        update.eq(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.PENDING.getCode())
                .set(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.REJECTED.getCode())
                .set(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.DISABLED.getCode())
                .set(MerchantInfoEntity::getAuditAdminId, adminUserId)
                .set(MerchantInfoEntity::getAuditTime, now)
                .set(MerchantInfoEntity::getAuditRemark, null)
                .set(MerchantInfoEntity::getRejectReason, reason)
                .set(MerchantInfoEntity::getUpdateTime, now);
        if (merchantInfoMapper.update(null, update) != 1) {
            handleAuditFailure(merchantId);
        }

        updateStoreState(
                merchantId,
                MerchantStatusEnum.DISABLED.getCode(),
                BusinessStatusEnum.CLOSED.getCode(),
                now);
        insertAuditLog(
                merchantId,
                adminUserId,
                MerchantAuditActionEnum.REJECT,
                current.getAuditStatus(),
                MerchantAuditStatusEnum.REJECTED.getCode(),
                reason,
                now);
        return buildOperationVO(merchantId, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMerchantOperationVO enable(Long merchantId) {
        validateMerchantId(merchantId);
        Long adminUserId = getCurrentUserId();
        MerchantInfoEntity current = getMerchant(merchantId);
        validateApprovedMerchantStatus(
                current,
                MerchantStatusEnum.DISABLED);

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantInfoEntity> update =
                baseMerchantUpdate(merchantId);
        update.eq(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.APPROVED.getCode())
                .eq(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.DISABLED.getCode())
                .set(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.ENABLED.getCode())
                .set(MerchantInfoEntity::getUpdateTime, now);
        if (merchantInfoMapper.update(null, update) != 1) {
            handleStatusFailure(merchantId);
        }

        updateStoreState(
                merchantId,
                MerchantStatusEnum.ENABLED.getCode(),
                null,
                now);
        insertAuditLog(
                merchantId,
                adminUserId,
                MerchantAuditActionEnum.ENABLE,
                current.getMerchantStatus(),
                MerchantStatusEnum.ENABLED.getCode(),
                "管理员启用商家",
                now);
        return buildOperationVO(merchantId, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMerchantOperationVO disable(
            Long merchantId,
            AdminMerchantReasonDTO request) {
        validateMerchantId(merchantId);
        String reason = validateReason(request);
        Long adminUserId = getCurrentUserId();
        MerchantInfoEntity current = getMerchant(merchantId);
        validateApprovedMerchantStatus(
                current,
                MerchantStatusEnum.ENABLED);

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<MerchantInfoEntity> update =
                baseMerchantUpdate(merchantId);
        update.eq(
                        MerchantInfoEntity::getAuditStatus,
                        MerchantAuditStatusEnum.APPROVED.getCode())
                .eq(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.ENABLED.getCode())
                .set(
                        MerchantInfoEntity::getMerchantStatus,
                        MerchantStatusEnum.DISABLED.getCode())
                .set(MerchantInfoEntity::getUpdateTime, now);
        if (merchantInfoMapper.update(null, update) != 1) {
            handleStatusFailure(merchantId);
        }

        updateStoreState(
                merchantId,
                MerchantStatusEnum.DISABLED.getCode(),
                BusinessStatusEnum.CLOSED.getCode(),
                now);
        insertAuditLog(
                merchantId,
                adminUserId,
                MerchantAuditActionEnum.DISABLE,
                current.getMerchantStatus(),
                MerchantStatusEnum.DISABLED.getCode(),
                reason,
                now);
        return buildOperationVO(merchantId, now);
    }

    private AdminMerchantPageQueryDTO validatePageQuery(
            AdminMerchantPageQueryDTO query) {
        AdminMerchantPageQueryDTO safe =
                query == null ? new AdminMerchantPageQueryDTO() : query;
        if (safe.getPageNum() == null
                || safe.getPageNum() < 1
                || safe.getPageSize() == null
                || safe.getPageSize() < 1
                || safe.getPageSize() > 100
                || (safe.getAuditStatus() != null
                    && MerchantAuditStatusEnum.of(
                            safe.getAuditStatus()) == null)
                || (safe.getMerchantStatus() != null
                    && MerchantStatusEnum.of(
                            safe.getMerchantStatus()) == null)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        safe.setKeyword(normalizeOptionalText(
                safe.getKeyword(),
                100));
        return safe;
    }

    private void validateMerchantId(Long merchantId) {
        if (merchantId == null || merchantId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private String validateAuditRemark(ApproveMerchantDTO request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String auditRemark = normalizeRequiredText(
                request.getAuditRemark(),
                1,
                TEXT_MAX_LENGTH);
        request.setAuditRemark(auditRemark);
        return auditRemark;
    }

    private String validateReason(AdminMerchantReasonDTO request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String reason = normalizeRequiredText(
                request.getReason(),
                REASON_MIN_LENGTH,
                TEXT_MAX_LENGTH);
        request.setReason(reason);
        return reason;
    }

    private String normalizeRequiredText(
            String value,
            int minLength,
            int maxLength) {
        if (value == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String normalized = value.trim();
        if (normalized.length() < minLength
                || normalized.length() > maxLength) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private LambdaUpdateWrapper<MerchantInfoEntity> baseMerchantUpdate(
            Long merchantId) {
        LambdaUpdateWrapper<MerchantInfoEntity> update =
                new LambdaUpdateWrapper<>();
        update.eq(MerchantInfoEntity::getId, merchantId)
                .eq(MerchantInfoEntity::getIsDeleted, NOT_DELETED);
        return update;
    }

    private MerchantInfoEntity getMerchant(Long merchantId) {
        MerchantInfoEntity merchant = findMerchant(merchantId);
        if (merchant == null) {
            throw new BusinessException(
                    ResultCode.ADMIN_MERCHANT_NOT_EXIST);
        }
        return merchant;
    }

    private MerchantInfoEntity findMerchant(Long merchantId) {
        LambdaQueryWrapper<MerchantInfoEntity> query =
                new LambdaQueryWrapper<>();
        query.eq(MerchantInfoEntity::getId, merchantId)
                .eq(MerchantInfoEntity::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1");
        return merchantInfoMapper.selectOne(query);
    }

    private MerchantStoreEntity getStore(Long merchantId) {
        LambdaQueryWrapper<MerchantStoreEntity> query =
                new LambdaQueryWrapper<>();
        query.eq(MerchantStoreEntity::getMerchantId, merchantId)
                .eq(MerchantStoreEntity::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1");
        MerchantStoreEntity store =
                merchantStoreMapper.selectOne(query);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        return store;
    }

    private void validateApprovedMerchantStatus(
            MerchantInfoEntity merchant,
            MerchantStatusEnum requiredStatus) {
        if (!MerchantAuditStatusEnum.APPROVED.getCode()
                .equals(merchant.getAuditStatus())) {
            throw new BusinessException(
                    ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        }
        if (!requiredStatus.getCode().equals(
                merchant.getMerchantStatus())) {
            throw new BusinessException(
                    ResultCode.MERCHANT_STATUS_ERROR);
        }
    }

    private void handleAuditFailure(Long merchantId) {
        MerchantInfoEntity latest = findMerchant(merchantId);
        if (latest == null) {
            throw new BusinessException(
                    ResultCode.ADMIN_MERCHANT_NOT_EXIST);
        }
        throw new BusinessException(
                ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
    }

    private void handleStatusFailure(Long merchantId) {
        MerchantInfoEntity latest = findMerchant(merchantId);
        if (latest == null) {
            throw new BusinessException(
                    ResultCode.ADMIN_MERCHANT_NOT_EXIST);
        }
        if (!MerchantAuditStatusEnum.APPROVED.getCode()
                .equals(latest.getAuditStatus())) {
            throw new BusinessException(
                    ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        }
        throw new BusinessException(ResultCode.MERCHANT_STATUS_ERROR);
    }

    private void updateStoreState(
            Long merchantId,
            Integer storeStatus,
            Integer businessStatus,
            LocalDateTime now) {
        LambdaUpdateWrapper<MerchantStoreEntity> update =
                new LambdaUpdateWrapper<>();
        update.eq(MerchantStoreEntity::getMerchantId, merchantId)
                .eq(MerchantStoreEntity::getIsDeleted, NOT_DELETED)
                .set(MerchantStoreEntity::getStoreStatus, storeStatus)
                .set(
                        businessStatus != null,
                        MerchantStoreEntity::getBusinessStatus,
                        businessStatus)
                .set(MerchantStoreEntity::getUpdateTime, now);
        if (merchantStoreMapper.update(null, update) == 1) {
            return;
        }

        MerchantStoreEntity store = getStore(merchantId);
        if (!storeStatus.equals(store.getStoreStatus())
                || (businessStatus != null
                    && !businessStatus.equals(
                            store.getBusinessStatus()))) {
            throw new IllegalStateException(
                    "merchant store status update failed");
        }
    }

    private void insertAuditLog(
            Long merchantId,
            Long adminUserId,
            MerchantAuditActionEnum action,
            Integer beforeStatus,
            Integer afterStatus,
            String reason,
            LocalDateTime now) {
        MerchantAuditLogEntity log = new MerchantAuditLogEntity();
        log.setMerchantId(merchantId);
        log.setAdminUserId(adminUserId);
        log.setAction(action.getCode());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setReason(reason);
        log.setCreateTime(now);
        if (merchantAuditLogMapper.insert(log) != 1) {
            throw new IllegalStateException(
                    "merchant audit log save failed");
        }
    }

    private AdminMerchantOperationVO buildOperationVO(
            Long merchantId,
            LocalDateTime operationTime) {
        MerchantInfoEntity merchant = getMerchant(merchantId);
        MerchantStoreEntity store = getStore(merchantId);
        AdminMerchantOperationVO result =
                new AdminMerchantOperationVO();
        result.setMerchantId(merchantId);
        result.setAuditStatus(merchant.getAuditStatus());
        result.setAuditStatusText(
                MerchantAuditStatusEnum.getTextByCode(
                        merchant.getAuditStatus()));
        result.setMerchantStatus(merchant.getMerchantStatus());
        result.setMerchantStatusText(
                MerchantStatusEnum.getTextByCode(
                        merchant.getMerchantStatus()));
        result.setStoreStatus(store.getStoreStatus());
        result.setStoreStatusText(
                MerchantStatusEnum.getTextByCode(
                        store.getStoreStatus()));
        result.setBusinessStatus(store.getBusinessStatus());
        result.setBusinessStatusText(
                BusinessStatusEnum.getTextByCode(
                        store.getBusinessStatus()));
        result.setAuditAdminId(merchant.getAuditAdminId());
        result.setAuditTime(merchant.getAuditTime());
        result.setAuditRemark(merchant.getAuditRemark());
        result.setRejectReason(merchant.getRejectReason());
        result.setOperationTime(operationTime);
        return result;
    }

    private void enrichStatusText(AdminMerchantPageVO result) {
        result.setAuditStatusText(
                MerchantAuditStatusEnum.getTextByCode(
                        result.getAuditStatus()));
        result.setMerchantStatusText(
                MerchantStatusEnum.getTextByCode(
                        result.getMerchantStatus()));
        result.setStoreStatusText(
                MerchantStatusEnum.getTextByCode(
                        result.getStoreStatus()));
        result.setBusinessStatusText(
                BusinessStatusEnum.getTextByCode(
                        result.getBusinessStatus()));
    }

    private PageResultVO<AdminMerchantPageVO> toPageResult(
            Page<AdminMerchantPageVO> page) {
        PageResultVO<AdminMerchantPageVO> result =
                new PageResultVO<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
