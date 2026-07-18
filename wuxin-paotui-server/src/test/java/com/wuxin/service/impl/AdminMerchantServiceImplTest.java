package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminMerchantPageQueryDTO;
import com.wuxin.dto.admin.AdminMerchantReasonDTO;
import com.wuxin.dto.admin.ApproveMerchantDTO;
import com.wuxin.entity.MerchantAuditLogEntity;
import com.wuxin.entity.MerchantInfoEntity;
import com.wuxin.entity.MerchantStoreEntity;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.enums.MerchantAuditStatusEnum;
import com.wuxin.enums.MerchantStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.AdminMerchantMapper;
import com.wuxin.mapper.MerchantAuditLogMapper;
import com.wuxin.mapper.MerchantInfoMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.AdminMerchantOperationVO;
import com.wuxin.vo.AdminMerchantPageVO;
import com.wuxin.vo.PageResultVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminMerchantServiceImplTest {

    private AdminMerchantMapper adminMerchantMapper;

    private MerchantInfoMapper merchantInfoMapper;

    private MerchantStoreMapper merchantStoreMapper;

    private MerchantAuditLogMapper merchantAuditLogMapper;

    private AdminMerchantServiceImpl service;

    @BeforeAll
    static void initializeMyBatisPlusMetadata() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(
                        new MybatisConfiguration(),
                        "admin-merchant-test");
        TableInfoHelper.initTableInfo(
                assistant,
                MerchantInfoEntity.class);
        TableInfoHelper.initTableInfo(
                assistant,
                MerchantStoreEntity.class);
    }

    @BeforeEach
    void setUp() {
        adminMerchantMapper = mock(AdminMerchantMapper.class);
        merchantInfoMapper = mock(MerchantInfoMapper.class);
        merchantStoreMapper = mock(MerchantStoreMapper.class);
        merchantAuditLogMapper = mock(MerchantAuditLogMapper.class);
        service = new AdminMerchantServiceImpl(
                adminMerchantMapper,
                merchantInfoMapper,
                merchantStoreMapper,
                merchantAuditLogMapper);
        UserContext.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void administratorShouldQueryMerchantApplications() {
        AdminMerchantPageVO record = new AdminMerchantPageVO();
        record.setMerchantId(10L);
        record.setAuditStatus(
                MerchantAuditStatusEnum.PENDING.getCode());
        record.setMerchantStatus(
                MerchantStatusEnum.ENABLED.getCode());
        record.setStoreStatus(
                MerchantStatusEnum.ENABLED.getCode());
        record.setBusinessStatus(
                BusinessStatusEnum.CLOSED.getCode());
        Page<AdminMerchantPageVO> page =
                new Page<>(1, 10, 1);
        page.setRecords(List.of(record));
        when(adminMerchantMapper.selectMerchantPage(
                any(Page.class),
                eq(null),
                eq(null),
                eq(null))).thenReturn(page);

        PageResultVO<AdminMerchantPageVO> result =
                service.page(new AdminMerchantPageQueryDTO());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst()
                .getAuditStatusText()).isEqualTo("待审核");
    }

    @Test
    void approveShouldUpdateMerchantStoreAndWriteLog() {
        mockSuccessfulOperation(
                merchant(
                        MerchantAuditStatusEnum.PENDING,
                        MerchantStatusEnum.ENABLED),
                merchant(
                        MerchantAuditStatusEnum.APPROVED,
                        MerchantStatusEnum.ENABLED),
                store(
                        MerchantStatusEnum.ENABLED,
                        BusinessStatusEnum.CLOSED));
        ApproveMerchantDTO request = new ApproveMerchantDTO();
        request.setAuditRemark("  资料审核通过  ");

        AdminMerchantOperationVO result =
                service.approve(10L, request);

        assertThat(result.getAuditStatus())
                .isEqualTo(
                        MerchantAuditStatusEnum.APPROVED.getCode());
        assertThat(result.getBusinessStatus())
                .isEqualTo(BusinessStatusEnum.CLOSED.getCode());
        ArgumentCaptor<MerchantAuditLogEntity> logCaptor =
                ArgumentCaptor.forClass(
                        MerchantAuditLogEntity.class);
        verify(merchantAuditLogMapper).insert(
                logCaptor.capture());
        assertThat(logCaptor.getValue().getAction())
                .isEqualTo("APPROVE");
        assertThat(logCaptor.getValue().getReason())
                .isEqualTo("资料审核通过");
    }

    @Test
    void concurrentApproveShouldOnlyWriteOneLog() {
        MerchantInfoEntity pending = merchant(
                MerchantAuditStatusEnum.PENDING,
                MerchantStatusEnum.ENABLED);
        MerchantInfoEntity approved = merchant(
                MerchantAuditStatusEnum.APPROVED,
                MerchantStatusEnum.ENABLED);
        when(merchantInfoMapper.selectOne(
                any(LambdaQueryWrapper.class)))
                .thenReturn(
                        pending,
                        approved,
                        pending,
                        approved);
        when(merchantInfoMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)))
                .thenReturn(1, 0);
        when(merchantStoreMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class)))
                .thenReturn(1);
        when(merchantStoreMapper.selectOne(
                any(LambdaQueryWrapper.class)))
                .thenReturn(store(
                        MerchantStatusEnum.ENABLED,
                        BusinessStatusEnum.CLOSED));
        when(merchantAuditLogMapper.insert(
                any(MerchantAuditLogEntity.class))).thenReturn(1);
        ApproveMerchantDTO request = new ApproveMerchantDTO();
        request.setAuditRemark("资料审核通过");

        service.approve(10L, request);

        assertBusinessError(
                () -> service.approve(10L, request),
                ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        verify(merchantAuditLogMapper, times(1))
                .insert(any(MerchantAuditLogEntity.class));
    }

    @Test
    void rejectShouldDisableMerchantAndStore() {
        mockSuccessfulOperation(
                merchant(
                        MerchantAuditStatusEnum.PENDING,
                        MerchantStatusEnum.ENABLED),
                rejectedMerchant(),
                store(
                        MerchantStatusEnum.DISABLED,
                        BusinessStatusEnum.CLOSED));
        AdminMerchantReasonDTO request =
                new AdminMerchantReasonDTO();
        request.setReason("营业执照信息不清晰");

        AdminMerchantOperationVO result =
                service.reject(10L, request);

        assertThat(result.getAuditStatus())
                .isEqualTo(
                        MerchantAuditStatusEnum.REJECTED.getCode());
        assertThat(result.getMerchantStatus())
                .isEqualTo(MerchantStatusEnum.DISABLED.getCode());
        assertThat(result.getStoreStatus())
                .isEqualTo(MerchantStatusEnum.DISABLED.getCode());
        assertThat(result.getRejectReason())
                .isEqualTo("营业执照信息不清晰");
    }

    @Test
    void emptyRejectReasonShouldFailValidation() {
        AdminMerchantReasonDTO request =
                new AdminMerchantReasonDTO();
        request.setReason(" ");

        assertBusinessError(
                () -> service.reject(10L, request),
                ResultCode.PARAM_ERROR);
        verify(merchantInfoMapper, never())
                .update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void unapprovedMerchantShouldNotBeEnabled() {
        when(merchantInfoMapper.selectOne(
                any(LambdaQueryWrapper.class)))
                .thenReturn(merchant(
                        MerchantAuditStatusEnum.PENDING,
                        MerchantStatusEnum.DISABLED));

        assertBusinessError(
                () -> service.enable(10L),
                ResultCode.MERCHANT_AUDIT_STATUS_ERROR);
        verify(merchantAuditLogMapper, never())
                .insert(any(MerchantAuditLogEntity.class));
    }

    @Test
    void disableShouldDisableStoreAndCloseBusiness() {
        mockSuccessfulOperation(
                merchant(
                        MerchantAuditStatusEnum.APPROVED,
                        MerchantStatusEnum.ENABLED),
                merchant(
                        MerchantAuditStatusEnum.APPROVED,
                        MerchantStatusEnum.DISABLED),
                store(
                        MerchantStatusEnum.DISABLED,
                        BusinessStatusEnum.CLOSED));
        AdminMerchantReasonDTO request =
                new AdminMerchantReasonDTO();
        request.setReason("存在违规经营行为");

        AdminMerchantOperationVO result =
                service.disable(10L, request);

        assertThat(result.getMerchantStatus())
                .isEqualTo(MerchantStatusEnum.DISABLED.getCode());
        assertThat(result.getStoreStatus())
                .isEqualTo(MerchantStatusEnum.DISABLED.getCode());
        assertThat(result.getBusinessStatus())
                .isEqualTo(BusinessStatusEnum.CLOSED.getCode());
        ArgumentCaptor<LambdaUpdateWrapper<MerchantStoreEntity>>
                updateCaptor = ArgumentCaptor.forClass(
                        LambdaUpdateWrapper.class);
        verify(merchantStoreMapper).update(
                eq(null),
                updateCaptor.capture());
        assertThat(updateCaptor.getValue().getSqlSet())
                .contains("store_status")
                .contains("business_status");
    }

    @Test
    void missingMerchantShouldReturnClearError() {
        when(merchantInfoMapper.selectOne(
                any(LambdaQueryWrapper.class))).thenReturn(null);

        assertBusinessError(
                () -> service.enable(999L),
                ResultCode.ADMIN_MERCHANT_NOT_EXIST);
    }

    @Test
    void invalidMerchantIdShouldReturnParameterError() {
        assertBusinessError(
                () -> service.detail(0L),
                ResultCode.PARAM_ERROR);
    }

    private void mockSuccessfulOperation(
            MerchantInfoEntity before,
            MerchantInfoEntity after,
            MerchantStoreEntity store) {
        when(merchantInfoMapper.selectOne(
                any(LambdaQueryWrapper.class)))
                .thenReturn(before, after);
        when(merchantInfoMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(merchantStoreMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(merchantStoreMapper.selectOne(
                any(LambdaQueryWrapper.class))).thenReturn(store);
        when(merchantAuditLogMapper.insert(
                any(MerchantAuditLogEntity.class))).thenReturn(1);
    }

    private MerchantInfoEntity rejectedMerchant() {
        MerchantInfoEntity merchant = merchant(
                MerchantAuditStatusEnum.REJECTED,
                MerchantStatusEnum.DISABLED);
        merchant.setRejectReason("营业执照信息不清晰");
        return merchant;
    }

    private MerchantInfoEntity merchant(
            MerchantAuditStatusEnum auditStatus,
            MerchantStatusEnum merchantStatus) {
        MerchantInfoEntity merchant = new MerchantInfoEntity();
        merchant.setId(10L);
        merchant.setUserId(2L);
        merchant.setAuditStatus(auditStatus.getCode());
        merchant.setMerchantStatus(merchantStatus.getCode());
        merchant.setAuditAdminId(1L);
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setIsDeleted(0);
        return merchant;
    }

    private MerchantStoreEntity store(
            MerchantStatusEnum storeStatus,
            BusinessStatusEnum businessStatus) {
        MerchantStoreEntity store = new MerchantStoreEntity();
        store.setId(20L);
        store.setMerchantId(10L);
        store.setStoreStatus(storeStatus.getCode());
        store.setBusinessStatus(businessStatus.getCode());
        store.setIsDeleted(0);
        return store;
    }

    private void assertBusinessError(
            Runnable action,
            ResultCode resultCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(
                                exception.getResultCode())
                                .isEqualTo(resultCode));
    }
}
