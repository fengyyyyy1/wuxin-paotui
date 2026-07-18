package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.AcceptOrderVO;
import com.wuxin.vo.GiveUpOrderVO;
import com.wuxin.vo.HallOrderVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiderOrderServiceImplTest {

    private OrderMapper orderMapper;

    private RiderInfoMapper riderInfoMapper;

    private OrderLogMapper orderLogMapper;

    private RiderOrderServiceImpl service;

    @BeforeAll
    static void initializeMyBatisPlusMetadata() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, OrderEntity.class);
    }

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        riderInfoMapper = mock(RiderInfoMapper.class);
        orderLogMapper = mock(OrderLogMapper.class);
        service = new RiderOrderServiceImpl(
                orderMapper, riderInfoMapper, orderLogMapper);
        UserContext.setUserId(2L);
        when(riderInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(activeRider());
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void hallQueryShouldIncludeReadyProductsAndWaitingErrands() {
        Page<OrderEntity> page = new Page<>(1, 10);
        page.setRecords(List.of());
        when(orderMapper.selectPage(
                any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        service.getHallOrders(1, 10);

        ArgumentCaptor<LambdaQueryWrapper<OrderEntity>> queryCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(orderMapper).selectPage(any(Page.class), queryCaptor.capture());
        String sql = queryCaptor.getValue().getCustomSqlSegment();
        assertThat(sql)
                .contains("order_type")
                .contains("status")
                .contains("pay_status")
                .contains("deleted");
        assertThat(queryCaptor.getValue().getParamNameValuePairs().values())
                .contains(
                        OrderTypeEnum.ERRAND.getCode(),
                        OrderStatusEnum.WAITING_ACCEPT.getCode(),
                        OrderTypeEnum.PRODUCT.getCode(),
                        OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode());
    }

    @Test
    void productOrderBeforeReadyShouldNotBeAcceptedByRider() {
        when(orderMapper.selectById(6L))
                .thenReturn(productOrder(OrderStatusEnum.WAITING_ACCEPT));

        assertThatThrownBy(() -> service.acceptOrder(6L))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.ORDER_STATUS_ERROR));
        verify(orderMapper, never()).update(
                eq(null), any(LambdaUpdateWrapper.class));
        verify(orderLogMapper, never()).insert(any(OrderLogEntity.class));
    }

    @Test
    void readyProductOrderShouldBeAcceptedFromStatusSeven() {
        when(orderMapper.selectById(6L))
                .thenReturn(productOrder(OrderStatusEnum.WAITING_RIDER_ACCEPT));
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);

        AcceptOrderVO result = service.acceptOrder(6L);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatusEnum.ACCEPTED.getCode());
        ArgumentCaptor<OrderLogEntity> logCaptor =
                ArgumentCaptor.forClass(OrderLogEntity.class);
        verify(orderLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getOldStatus())
                .isEqualTo(OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode());
    }

    @Test
    void waitingErrandShouldKeepExistingAcceptFlow() {
        when(orderMapper.selectById(1L))
                .thenReturn(errandOrder(OrderStatusEnum.WAITING_ACCEPT));
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);

        AcceptOrderVO result = service.acceptOrder(1L);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatusEnum.ACCEPTED.getCode());
        ArgumentCaptor<OrderLogEntity> logCaptor =
                ArgumentCaptor.forClass(OrderLogEntity.class);
        verify(orderLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getOldStatus())
                .isEqualTo(OrderStatusEnum.WAITING_ACCEPT.getCode());
    }

    @Test
    void productGiveUpShouldReturnToRiderHallStatus() {
        OrderEntity order = productOrder(OrderStatusEnum.ACCEPTED);
        order.setRiderId(8L);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(order);
        when(orderMapper.update(
                eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderLogMapper.insert(any(OrderLogEntity.class))).thenReturn(1);

        GiveUpOrderVO result = service.giveUpOrder(6L);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode());
        ArgumentCaptor<OrderLogEntity> logCaptor =
                ArgumentCaptor.forClass(OrderLogEntity.class);
        verify(orderLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getNewStatus())
                .isEqualTo(OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode());
    }

    private RiderInfoEntity activeRider() {
        RiderInfoEntity rider = new RiderInfoEntity();
        rider.setId(8L);
        rider.setUserId(2L);
        rider.setAuditStatus(1);
        rider.setRiderStatus(1);
        return rider;
    }

    private OrderEntity productOrder(OrderStatusEnum status) {
        OrderEntity order = new OrderEntity();
        order.setId(6L);
        order.setOrderType(OrderTypeEnum.PRODUCT.getCode());
        order.setStatus(status.getCode());
        order.setPayStatus(PaymentStatusEnum.PAID.getCode());
        order.setDeleted(0);
        return order;
    }

    private OrderEntity errandOrder(OrderStatusEnum status) {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setOrderType(OrderTypeEnum.ERRAND.getCode());
        order.setStatus(status.getCode());
        order.setPayStatus(PaymentStatusEnum.PAID.getCode());
        order.setDeleted(0);
        return order;
    }
}
