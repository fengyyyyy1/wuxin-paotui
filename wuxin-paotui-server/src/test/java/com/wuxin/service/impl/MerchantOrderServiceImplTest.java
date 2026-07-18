package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.merchant.MerchantOrderPageQueryDTO;
import com.wuxin.dto.merchant.RejectMerchantOrderDTO;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderItemEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantOrderMapper;
import com.wuxin.mapper.OrderItemMapper;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.service.MerchantService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.MerchantOrderDetailVO;
import com.wuxin.vo.MerchantOrderPageVO;
import com.wuxin.vo.MerchantOrderStatusVO;
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

class MerchantOrderServiceImplTest {

    private MerchantService merchantService;

    private MerchantOrderMapper merchantOrderMapper;

    private OrderMapper orderMapper;

    private OrderItemMapper orderItemMapper;

    private OrderLogMapper orderLogMapper;

    private MerchantOrderServiceImpl service;

    @BeforeAll
    static void initializeMyBatisPlusMetadata() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, OrderEntity.class);
    }

    @BeforeEach
    void setUp() {
        merchantService = mock(MerchantService.class);
        merchantOrderMapper = mock(MerchantOrderMapper.class);
        orderMapper = mock(OrderMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        orderLogMapper = mock(OrderLogMapper.class);
        service = new MerchantOrderServiceImpl(
                merchantService,
                merchantOrderMapper,
                orderMapper,
                orderItemMapper,
                orderLogMapper);
        UserContext.setUserId(20L);
        when(merchantService.getCurrentApprovedStoreId()).thenReturn(10L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void nonMerchantShouldNotAccessOrders() {
        when(merchantService.getCurrentApprovedStoreId())
                .thenThrow(new BusinessException(ResultCode.MERCHANT_NOT_EXIST));

        assertBusinessError(
                () -> service.pageOrders(new MerchantOrderPageQueryDTO()),
                ResultCode.MERCHANT_NOT_EXIST);
        verify(merchantOrderMapper, never()).selectOrderPage(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void pageShouldUseCurrentMerchantStoreAndMaskPhone() {
        MerchantOrderPageVO record = new MerchantOrderPageVO();
        record.setOrderId(6L);
        record.setStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        record.setPayStatus(PaymentStatusEnum.PAID.getCode());
        record.setReceiverPhone("13800000003");
        Page<MerchantOrderPageVO> page = new Page<>(1, 10);
        page.setRecords(List.of(record));
        page.setTotal(1);
        when(merchantOrderMapper.selectOrderPage(
                any(), eq(10L), eq(OrderTypeEnum.PRODUCT.getCode()),
                eq(null), eq(null), eq(null), eq(null))).thenReturn(page);

        PageResultVO<MerchantOrderPageVO> result =
                service.pageOrders(new MerchantOrderPageQueryDTO());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().getFirst().getReceiverPhone())
                .isEqualTo("138****0003");
        assertThat(result.getRecords().getFirst().getStatusName())
                .isEqualTo("待商家接单");
    }

    @Test
    void detailShouldHideOtherStoreOrder() {
        when(merchantOrderMapper.selectOrderDetail(
                9L, 10L, OrderTypeEnum.PRODUCT.getCode())).thenReturn(null);

        assertBusinessError(
                () -> service.getOrderDetail(9L),
                ResultCode.MERCHANT_ORDER_NOT_EXIST);
    }

    @Test
    void detailShouldUseOrderItemSnapshotAndMaskPhone() {
        MerchantOrderDetailVO detail = new MerchantOrderDetailVO();
        detail.setOrderId(6L);
        detail.setStatus(OrderStatusEnum.MERCHANT_PREPARING.getCode());
        detail.setPayStatus(PaymentStatusEnum.PAID.getCode());
        detail.setReceiverPhone("13800000003");
        when(merchantOrderMapper.selectOrderDetail(
                6L, 10L, OrderTypeEnum.PRODUCT.getCode())).thenReturn(detail);

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(3L);
        item.setProductName("下单时商品快照");
        item.setQuantity(2);
        when(orderItemMapper.selectByOrderId(6L)).thenReturn(List.of(item));
        when(orderLogMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        MerchantOrderDetailVO result = service.getOrderDetail(6L);

        assertThat(result.getReceiverPhone()).isEqualTo("138****0003");
        assertThat(result.getItems()).singleElement()
                .extracting("productName")
                .isEqualTo("下单时商品快照");
    }

    @Test
    void unpaidOrderAndErrandOrderShouldNotBeAccepted() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(productOrder(
                        OrderStatusEnum.WAITING_ACCEPT,
                        PaymentStatusEnum.UNPAID));

        assertBusinessError(
                () -> service.acceptOrder(6L),
                ResultCode.MERCHANT_ORDER_UNPAID);
        verify(orderLogMapper, never()).insert(any(OrderLogEntity.class));

        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertBusinessError(
                () -> service.acceptOrder(7L),
                ResultCode.MERCHANT_ORDER_NOT_EXIST);
    }

    @Test
    void concurrentAcceptShouldWriteOnlyOneLog() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1, 0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(productOrder(
                        OrderStatusEnum.MERCHANT_PREPARING,
                        PaymentStatusEnum.PAID));
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);

        MerchantOrderStatusVO first = service.acceptOrder(6L);

        assertThat(first.getStatus())
                .isEqualTo(OrderStatusEnum.MERCHANT_PREPARING.getCode());
        assertBusinessError(
                () -> service.acceptOrder(6L),
                ResultCode.MERCHANT_ORDER_STATUS_ERROR);
        verify(orderLogMapper, times(1)).insert(any(OrderLogEntity.class));

        ArgumentCaptor<OrderLogEntity> logCaptor =
                ArgumentCaptor.forClass(OrderLogEntity.class);
        verify(orderLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getOldStatus())
                .isEqualTo(OrderStatusEnum.WAITING_ACCEPT.getCode());
        assertThat(logCaptor.getValue().getNewStatus())
                .isEqualTo(OrderStatusEnum.MERCHANT_PREPARING.getCode());
        assertThat(logCaptor.getValue().getOperatorType()).isEqualTo("MERCHANT");
    }

    @Test
    void rejectShouldEnterWaitingRefundAndPersistReason() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);
        RejectMerchantOrderDTO request = new RejectMerchantOrderDTO();
        request.setReason("  商品暂时缺货  ");

        MerchantOrderStatusVO result = service.rejectOrder(6L, request);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatusEnum.WAITING_REFUND.getCode());
        assertThat(result.getRejectReason()).isEqualTo("商品暂时缺货");
        ArgumentCaptor<OrderLogEntity> logCaptor =
                ArgumentCaptor.forClass(OrderLogEntity.class);
        verify(orderLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getRemark())
                .isEqualTo("商家拒单：商品暂时缺货");
    }

    @Test
    void nonWaitingOrderShouldNotBeRejected() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(productOrder(
                        OrderStatusEnum.MERCHANT_PREPARING,
                        PaymentStatusEnum.PAID));
        RejectMerchantOrderDTO request = new RejectMerchantOrderDTO();
        request.setReason("商品暂时缺货");

        assertBusinessError(
                () -> service.rejectOrder(6L, request),
                ResultCode.MERCHANT_ORDER_STATUS_ERROR);
        verify(orderLogMapper, never()).insert(any(OrderLogEntity.class));
    }

    @Test
    void readyShouldRequireMerchantAcceptedAndNotDuplicateLog() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1, 0);
        LocalDateTime merchantAcceptTime =
                LocalDateTime.of(2026, 7, 18, 17, 45);
        LocalDateTime readyTime =
                LocalDateTime.of(2026, 7, 18, 17, 50);
        OrderEntity updatedOrder = productOrder(
                OrderStatusEnum.WAITING_RIDER_ACCEPT,
                PaymentStatusEnum.PAID);
        updatedOrder.setMerchantAcceptTime(merchantAcceptTime);
        updatedOrder.setMerchantReadyTime(readyTime);
        updatedOrder.setMerchantRejectTime(null);
        updatedOrder.setMerchantRejectReason(null);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(updatedOrder);
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);

        MerchantOrderStatusVO first = service.readyOrder(6L);

        assertThat(first.getStatus())
                .isEqualTo(OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode());
        assertThat(first.getMerchantAcceptTime()).isEqualTo(merchantAcceptTime);
        assertThat(first.getReadyTime()).isEqualTo(readyTime);
        assertThat(first.getRejectTime()).isNull();
        assertThat(first.getRejectReason()).isNull();
        assertBusinessError(
                () -> service.readyOrder(6L),
                ResultCode.MERCHANT_ORDER_STATUS_ERROR);
        verify(orderLogMapper, times(1)).insert(any(OrderLogEntity.class));
    }

    @Test
    void orderBeforeMerchantAcceptShouldNotBeMarkedReady() {
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(productOrder(
                        OrderStatusEnum.WAITING_ACCEPT,
                        PaymentStatusEnum.PAID));

        assertBusinessError(
                () -> service.readyOrder(6L),
                ResultCode.MERCHANT_ORDER_STATUS_ERROR);
        verify(orderLogMapper, never()).insert(any(OrderLogEntity.class));
    }

    private OrderEntity productOrder(
            OrderStatusEnum status,
            PaymentStatusEnum payStatus) {
        OrderEntity order = new OrderEntity();
        order.setId(6L);
        order.setStoreId(10L);
        order.setOrderType(OrderTypeEnum.PRODUCT.getCode());
        order.setStatus(status.getCode());
        order.setPayStatus(payStatus.getCode());
        order.setDeleted(0);
        return order;
    }

    private void assertBusinessError(
            Runnable action,
            ResultCode expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getResultCode())
                                .isEqualTo(expectedCode));
    }
}
