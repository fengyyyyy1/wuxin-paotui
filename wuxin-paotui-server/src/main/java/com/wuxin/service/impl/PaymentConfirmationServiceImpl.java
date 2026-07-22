package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.PaymentOrderEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentOrderStatusEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.PaymentOrderMapper;
import com.wuxin.service.PaymentConfirmationService;
import com.wuxin.service.model.PaymentSuccessCommand;
import com.wuxin.utils.PaymentAmountUtils;
import com.wuxin.vo.PaymentStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentConfirmationServiceImpl implements PaymentConfirmationService {

    private static final int NOT_DELETED = 0;

    private static final String OPERATOR_TYPE_USER = "USER";

    private final PaymentOrderMapper paymentOrderMapper;

    private final OrderMapper orderMapper;

    private final OrderLogMapper orderLogMapper;

    public PaymentConfirmationServiceImpl(
            PaymentOrderMapper paymentOrderMapper,
            OrderMapper orderMapper,
            OrderLogMapper orderLogMapper) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.orderMapper = orderMapper;
        this.orderLogMapper = orderLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentStatusVO confirmPaymentSuccess(PaymentSuccessCommand command) {
        validateCommand(command);
        PaymentOrderEntity payment = getPaymentOrder(command.getPaymentNo());
        OrderEntity order = getOrder(payment.getOrderId());
        validatePaymentData(payment, order, command);

        if (PaymentOrderStatusEnum.SUCCESS.getCode().equals(payment.getStatus())) {
            validateConfirmedPayment(payment, order, command);
            return toPaymentStatusVO(order, payment);
        }
        if (!List.of(
                        PaymentOrderStatusEnum.CREATED.getCode(),
                        PaymentOrderStatusEnum.WAITING_PAY.getCode())
                .contains(payment.getStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }
        if (PaymentStatusEnum.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }

        LocalDateTime successTime = command.getSuccessTime() == null
                ? LocalDateTime.now()
                : command.getSuccessTime();

        LambdaUpdateWrapper<PaymentOrderEntity> paymentUpdate = new LambdaUpdateWrapper<>();
        paymentUpdate.eq(PaymentOrderEntity::getId, payment.getId())
                .in(PaymentOrderEntity::getStatus,
                        PaymentOrderStatusEnum.CREATED.getCode(),
                        PaymentOrderStatusEnum.WAITING_PAY.getCode())
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .set(PaymentOrderEntity::getStatus, PaymentOrderStatusEnum.SUCCESS.getCode())
                .set(PaymentOrderEntity::getTransactionId, command.getTransactionId())
                .set(PaymentOrderEntity::getPayerTotal, command.getPayerTotal())
                .set(PaymentOrderEntity::getSuccessTime, successTime)
                .set(command.getNotifyId() != null,
                        PaymentOrderEntity::getNotifyId, command.getNotifyId())
                .set(command.getNotifyBodyHash() != null,
                        PaymentOrderEntity::getNotifyBodyHash, command.getNotifyBodyHash())
                .set(PaymentOrderEntity::getErrorCode, null)
                .set(PaymentOrderEntity::getErrorMessage, null)
                .set(PaymentOrderEntity::getUpdateTime, successTime)
                .setSql("version = version + 1");

        if (paymentOrderMapper.update(null, paymentUpdate) != 1) {
            PaymentOrderEntity current = getPaymentOrder(command.getPaymentNo());
            if (PaymentOrderStatusEnum.SUCCESS.getCode().equals(current.getStatus())) {
                OrderEntity currentOrder = getOrder(current.getOrderId());
                validateConfirmedPayment(current, currentOrder, command);
                return toPaymentStatusVO(currentOrder, current);
            }
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }

        LambdaUpdateWrapper<OrderEntity> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(OrderEntity::getId, order.getId())
                .eq(OrderEntity::getUserId, payment.getUserId())
                .eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_ACCEPT.getCode())
                .eq(OrderEntity::getPayStatus, PaymentStatusEnum.UNPAID.getCode())
                .eq(OrderEntity::getDeleted, NOT_DELETED)
                .set(OrderEntity::getPayStatus, PaymentStatusEnum.PAID.getCode())
                .set(OrderEntity::getPayTime, successTime)
                .set(OrderEntity::getPaymentNo, payment.getPaymentNo())
                .set(OrderEntity::getUpdateTime, successTime);

        if (orderMapper.update(null, orderUpdate) != 1) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(order.getId());
        orderLog.setOldStatus(order.getStatus());
        orderLog.setNewStatus(order.getStatus());
        orderLog.setOperatorId(payment.getUserId());
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("订单支付成功");
        orderLog.setCreateTime(successTime);
        if (orderLogMapper.insert(orderLog) != 1) {
            throw new IllegalStateException("order log save failed");
        }

        order.setPayStatus(PaymentStatusEnum.PAID.getCode());
        order.setPayTime(successTime);
        order.setPaymentNo(payment.getPaymentNo());
        payment.setStatus(PaymentOrderStatusEnum.SUCCESS.getCode());
        payment.setTransactionId(command.getTransactionId());
        payment.setPayerTotal(command.getPayerTotal());
        payment.setSuccessTime(successTime);
        return toPaymentStatusVO(order, payment);
    }

    private void validateCommand(PaymentSuccessCommand command) {
        if (command == null
                || command.getPaymentNo() == null
                || command.getPaymentNo().isBlank()
                || command.getTransactionId() == null
                || command.getTransactionId().isBlank()
                || command.getPayerTotal() == null
                || command.getPayerTotal() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }

    private PaymentOrderEntity getPaymentOrder(String paymentNo) {
        LambdaQueryWrapper<PaymentOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrderEntity::getPaymentNo, paymentNo)
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .last("LIMIT 1 FOR UPDATE");
        PaymentOrderEntity payment = paymentOrderMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_EXIST);
        }
        return payment;
    }

    private OrderEntity getOrder(Long orderId) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderEntity::getId, orderId)
                .eq(OrderEntity::getDeleted, NOT_DELETED)
                .last("LIMIT 1");
        OrderEntity order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    private void validatePaymentData(
            PaymentOrderEntity payment,
            OrderEntity order,
            PaymentSuccessCommand command) {
        Integer orderAmount;
        try {
            orderAmount = PaymentAmountUtils.yuanToFen(getPayableAmount(order));
        } catch (ArithmeticException exception) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_INVALID);
        }
        if (!payment.getOrderId().equals(order.getId())
                || !payment.getOrderNo().equals(order.getOrderNo())
                || !payment.getUserId().equals(order.getUserId())
                || !isPayableOrderType(order)
                || !payment.getAmountTotal().equals(orderAmount)
                || !payment.getAmountTotal().equals(command.getPayerTotal())) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_INVALID);
        }
    }

    private void validateConfirmedPayment(
            PaymentOrderEntity payment,
            OrderEntity order,
            PaymentSuccessCommand command) {
        if (!command.getTransactionId().equals(payment.getTransactionId())
                || !PaymentStatusEnum.PAID.getCode().equals(order.getPayStatus())
                || !payment.getPaymentNo().equals(order.getPaymentNo())) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }
    }

    private PaymentStatusVO toPaymentStatusVO(
            OrderEntity order, PaymentOrderEntity payment) {
        PaymentStatusVO result = new PaymentStatusVO();
        result.setOrderId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setPayStatus(order.getPayStatus());
        result.setPaymentNo(payment.getPaymentNo());
        result.setPaymentStatus(payment.getStatus());
        result.setPaymentStatusText(
                PaymentOrderStatusEnum.getDescriptionByCode(payment.getStatus()));
        result.setTransactionId(payment.getTransactionId());
        result.setAmountTotal(payment.getAmountTotal());
        result.setSuccessTime(payment.getSuccessTime());
        return result;
    }

    private boolean isPayableOrderType(OrderEntity order) {
        return OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())
                || OrderTypeEnum.ERRAND.getCode().equals(order.getOrderType())
                || order.getOrderType() == null;
    }

    private java.math.BigDecimal getPayableAmount(OrderEntity order) {
        if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            return order.getTotalAmount();
        }
        return order.getPrice();
    }
}
