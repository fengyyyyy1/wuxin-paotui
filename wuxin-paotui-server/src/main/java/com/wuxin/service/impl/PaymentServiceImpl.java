package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.config.MockPaymentProperties;
import com.wuxin.config.WeChatPayProperties;
import com.wuxin.dto.payment.CreateJsapiPaymentDTO;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.PaymentOrderEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentOrderStatusEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.PaymentGateway;
import com.wuxin.gateway.PaymentGatewayRouter;
import com.wuxin.gateway.model.PaymentGatewayCreateRequest;
import com.wuxin.gateway.model.PaymentGatewayCreateResult;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.PaymentOrderMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.PaymentConfirmationService;
import com.wuxin.service.PaymentService;
import com.wuxin.service.model.PaymentSuccessCommand;
import com.wuxin.utils.PaymentAmountUtils;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.JsapiPaymentVO;
import com.wuxin.vo.PaymentStatusVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final int NOT_DELETED = 0;

    private static final int PAYMENT_EXPIRE_MINUTES = 30;

    private static final String TRADE_TYPE_JSAPI = "JSAPI";

    private static final String CURRENCY_CNY = "CNY";

    private static final DateTimeFormatter PAYMENT_NO_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;

    private final PaymentOrderMapper paymentOrderMapper;

    private final UserMapper userMapper;

    private final PaymentGatewayRouter paymentGatewayRouter;

    private final PaymentConfirmationService paymentConfirmationService;

    private final WeChatPayProperties weChatPayProperties;

    private final MockPaymentProperties mockPaymentProperties;

    public PaymentServiceImpl(
            OrderMapper orderMapper,
            PaymentOrderMapper paymentOrderMapper,
            UserMapper userMapper,
            PaymentGatewayRouter paymentGatewayRouter,
            PaymentConfirmationService paymentConfirmationService,
            WeChatPayProperties weChatPayProperties,
            MockPaymentProperties mockPaymentProperties) {
        this.orderMapper = orderMapper;
        this.paymentOrderMapper = paymentOrderMapper;
        this.userMapper = userMapper;
        this.paymentGatewayRouter = paymentGatewayRouter;
        this.paymentConfirmationService = paymentConfirmationService;
        this.weChatPayProperties = weChatPayProperties;
        this.mockPaymentProperties = mockPaymentProperties;
    }

    @Override
    public JsapiPaymentVO createJsapiPayment(CreateJsapiPaymentDTO request) {
        Long userId = getCurrentUserId();
        if (request == null || request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        OrderEntity order = getOwnedOrder(request.getOrderId(), userId);
        validateOrderForPayment(order);
        Integer amountTotal = toAmountTotal(order);
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (weChatPayProperties.isEnabled()
                && (user.getOpenId() == null || user.getOpenId().isBlank())) {
            throw new BusinessException(ResultCode.USER_OPENID_MISSING);
        }

        PaymentOrderEntity activePayment = findActivePayment(order.getId(), userId);
        if (activePayment != null) {
            return reuseActivePayment(order, activePayment, amountTotal);
        }

        PaymentGateway gateway = paymentGatewayRouter.getActiveGateway();
        LocalDateTime now = LocalDateTime.now();
        PaymentOrderEntity payment = buildPaymentOrder(
                order, user, gateway.getType(), amountTotal, now);

        try {
            if (paymentOrderMapper.insert(payment) != 1 || payment.getId() == null) {
                throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            PaymentOrderEntity concurrentPayment = findActivePayment(order.getId(), userId);
            if (concurrentPayment != null) {
                return reuseActivePayment(order, concurrentPayment, amountTotal);
            }
            throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
        }

        PaymentGatewayCreateRequest gatewayRequest =
                buildGatewayRequest(order, payment, user.getOpenId());
        PaymentGatewayCreateResult gatewayResult;
        try {
            gatewayResult = gateway.createPayment(gatewayRequest);
            validateGatewayResult(gatewayResult);
        } catch (BusinessException exception) {
            markPaymentFailed(payment.getId(), "GATEWAY_CREATE_FAILED", "支付网关创建失败");
            throw exception;
        } catch (Exception exception) {
            markPaymentFailed(payment.getId(), "GATEWAY_CREATE_FAILED", "支付网关创建失败");
            throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
        }

        LambdaUpdateWrapper<PaymentOrderEntity> update = new LambdaUpdateWrapper<>();
        update.eq(PaymentOrderEntity::getId, payment.getId())
                .eq(PaymentOrderEntity::getStatus, PaymentOrderStatusEnum.CREATED.getCode())
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .set(PaymentOrderEntity::getStatus, PaymentOrderStatusEnum.WAITING_PAY.getCode())
                .set(PaymentOrderEntity::getPrepayId, gatewayResult.getPrepayId())
                .set(PaymentOrderEntity::getErrorCode, null)
                .set(PaymentOrderEntity::getErrorMessage, null)
                .set(PaymentOrderEntity::getUpdateTime, LocalDateTime.now())
                .setSql("version = version + 1");

        if (paymentOrderMapper.update(null, update) != 1) {
            recordCreationError(
                    payment.getId(),
                    "LOCAL_UPDATE_FAILED",
                    "支付网关创建成功，但本地支付状态更新失败");
            throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
        }

        return toJsapiPaymentVO(payment.getPaymentNo(), gatewayResult);
    }

    @Override
    public PaymentStatusVO getOrderPaymentStatus(Long orderId) {
        Long userId = getCurrentUserId();
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        OrderEntity order = getOwnedOrder(orderId, userId);
        PaymentOrderEntity payment = findLatestPayment(orderId, userId);
        return toPaymentStatusVO(order, payment);
    }

    @Override
    public PaymentStatusVO confirmMockPaymentSuccess(String paymentNo) {
        if (!mockPaymentProperties.isEnabled()) {
            throw new BusinessException(ResultCode.MOCK_PAYMENT_DISABLED);
        }
        Long userId = getCurrentUserId();
        if (paymentNo == null || paymentNo.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        PaymentOrderEntity payment = findOwnedPayment(paymentNo, userId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_EXIST);
        }
        if (!PaymentGatewayRouter.MOCK_GATEWAY.equals(payment.getPaymentChannel())) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_SUPPORTED);
        }

        PaymentSuccessCommand command = PaymentSuccessCommand.builder()
                .paymentNo(paymentNo)
                .transactionId("MOCK_TXN_" + paymentNo)
                .payerTotal(payment.getAmountTotal())
                .successTime(LocalDateTime.now())
                .notifyId("MOCK_NOTIFY_" + paymentNo)
                .build();
        return paymentConfirmationService.confirmPaymentSuccess(command);
    }

    private JsapiPaymentVO reuseActivePayment(
            OrderEntity order, PaymentOrderEntity payment, Integer amountTotal) {
        if (!payment.getAmountTotal().equals(amountTotal)
                || !payment.getOrderNo().equals(order.getOrderNo())) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_INVALID);
        }
        if (PaymentOrderStatusEnum.CREATED.getCode().equals(payment.getStatus())) {
            throw new BusinessException(
                    ResultCode.PAYMENT_CREATE_FAILED,
                    "支付单正在创建，请稍后重试");
        }
        if (!PaymentOrderStatusEnum.WAITING_PAY.getCode().equals(payment.getStatus())
                || payment.getPrepayId() == null
                || payment.getPrepayId().isBlank()) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }

        PaymentGateway gateway = paymentGatewayRouter.getGateway(payment.getPaymentChannel());
        PaymentGatewayCreateResult result = gateway.buildPaymentParameters(
                buildGatewayRequest(order, payment, payment.getOpenId()),
                payment.getPrepayId());
        validateGatewayResult(result);
        return toJsapiPaymentVO(payment.getPaymentNo(), result);
    }

    private void validateOrderForPayment(OrderEntity order) {
        if (!isPayableOrderType(order)) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_SUPPORTED);
        }
        if (!OrderStatusEnum.WAITING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_PAY);
        }
        if (PaymentStatusEnum.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }
        if (!PaymentStatusEnum.UNPAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_INVALID);
        }
    }

    private Integer toAmountTotal(OrderEntity order) {
        try {
            return PaymentAmountUtils.yuanToFen(getPayableAmount(order));
        } catch (ArithmeticException exception) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_INVALID);
        }
    }

    private PaymentOrderEntity buildPaymentOrder(
            OrderEntity order,
            UserEntity user,
            String gatewayType,
            Integer amountTotal,
            LocalDateTime now) {
        PaymentOrderEntity payment = new PaymentOrderEntity();
        payment.setPaymentNo(generatePaymentNo(now));
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setPaymentChannel(gatewayType);
        payment.setTradeType(TRADE_TYPE_JSAPI);
        payment.setAppId(nullIfBlank(weChatPayProperties.getAppId()));
        payment.setMchId(nullIfBlank(weChatPayProperties.getMchId()));
        payment.setOpenId(nullIfBlank(user.getOpenId()));
        payment.setAmountTotal(amountTotal);
        payment.setCurrency(CURRENCY_CNY);
        payment.setStatus(PaymentOrderStatusEnum.CREATED.getCode());
        payment.setExpireTime(now.plusMinutes(PAYMENT_EXPIRE_MINUTES));
        payment.setVersion(0);
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        payment.setDeleted(NOT_DELETED);
        return payment;
    }

    private PaymentGatewayCreateRequest buildGatewayRequest(
            OrderEntity order, PaymentOrderEntity payment, String openId) {
        String orderTypeText = OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())
                ? "商品订单"
                : "跑腿订单";
        return PaymentGatewayCreateRequest.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(order.getOrderNo())
                .description("五鑫跑腿" + orderTypeText + "-" + order.getOrderNo())
                .amountTotal(payment.getAmountTotal())
                .currency(payment.getCurrency())
                .appId(payment.getAppId())
                .mchId(payment.getMchId())
                .openId(openId)
                .notifyUrl(weChatPayProperties.getNotifyUrl())
                .build();
    }

    private OrderEntity getOwnedOrder(Long orderId, Long userId) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderEntity::getId, orderId)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, NOT_DELETED)
                .last("LIMIT 1");
        OrderEntity order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    private PaymentOrderEntity findActivePayment(Long orderId, Long userId) {
        LambdaQueryWrapper<PaymentOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrderEntity::getOrderId, orderId)
                .eq(PaymentOrderEntity::getUserId, userId)
                .in(PaymentOrderEntity::getStatus,
                        PaymentOrderStatusEnum.CREATED.getCode(),
                        PaymentOrderStatusEnum.WAITING_PAY.getCode())
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .orderByDesc(PaymentOrderEntity::getCreateTime)
                .last("LIMIT 1");
        return paymentOrderMapper.selectOne(wrapper);
    }

    private PaymentOrderEntity findLatestPayment(Long orderId, Long userId) {
        LambdaQueryWrapper<PaymentOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrderEntity::getOrderId, orderId)
                .eq(PaymentOrderEntity::getUserId, userId)
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .orderByDesc(PaymentOrderEntity::getCreateTime)
                .last("LIMIT 1");
        return paymentOrderMapper.selectOne(wrapper);
    }

    private PaymentOrderEntity findOwnedPayment(String paymentNo, Long userId) {
        LambdaQueryWrapper<PaymentOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrderEntity::getPaymentNo, paymentNo)
                .eq(PaymentOrderEntity::getUserId, userId)
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .last("LIMIT 1");
        return paymentOrderMapper.selectOne(wrapper);
    }

    private void markPaymentFailed(Long paymentId, String errorCode, String errorMessage) {
        LambdaUpdateWrapper<PaymentOrderEntity> update = new LambdaUpdateWrapper<>();
        update.eq(PaymentOrderEntity::getId, paymentId)
                .eq(PaymentOrderEntity::getStatus, PaymentOrderStatusEnum.CREATED.getCode())
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .set(PaymentOrderEntity::getStatus, PaymentOrderStatusEnum.FAILED.getCode())
                .set(PaymentOrderEntity::getErrorCode, errorCode)
                .set(PaymentOrderEntity::getErrorMessage, sanitizeErrorMessage(errorMessage))
                .set(PaymentOrderEntity::getUpdateTime, LocalDateTime.now())
                .setSql("version = version + 1");
        paymentOrderMapper.update(null, update);
    }

    private void recordCreationError(
            Long paymentId, String errorCode, String errorMessage) {
        LambdaUpdateWrapper<PaymentOrderEntity> update = new LambdaUpdateWrapper<>();
        update.eq(PaymentOrderEntity::getId, paymentId)
                .eq(PaymentOrderEntity::getDeleted, NOT_DELETED)
                .set(PaymentOrderEntity::getErrorCode, errorCode)
                .set(PaymentOrderEntity::getErrorMessage, sanitizeErrorMessage(errorMessage))
                .set(PaymentOrderEntity::getUpdateTime, LocalDateTime.now())
                .setSql("version = version + 1");
        paymentOrderMapper.update(null, update);
    }

    private void validateGatewayResult(PaymentGatewayCreateResult result) {
        if (result == null
                || isBlank(result.getPrepayId())
                || isBlank(result.getTimeStamp())
                || isBlank(result.getNonceStr())
                || isBlank(result.getPackageValue())
                || isBlank(result.getSignType())
                || isBlank(result.getPaySign())) {
            throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
        }
    }

    private JsapiPaymentVO toJsapiPaymentVO(
            String paymentNo, PaymentGatewayCreateResult gatewayResult) {
        JsapiPaymentVO result = new JsapiPaymentVO();
        result.setPaymentNo(paymentNo);
        result.setTimeStamp(gatewayResult.getTimeStamp());
        result.setNonceStr(gatewayResult.getNonceStr());
        result.setPackageValue(gatewayResult.getPackageValue());
        result.setSignType(gatewayResult.getSignType());
        result.setPaySign(gatewayResult.getPaySign());
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

    private PaymentStatusVO toPaymentStatusVO(
            OrderEntity order, PaymentOrderEntity payment) {
        PaymentStatusVO result = new PaymentStatusVO();
        result.setOrderId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setPayStatus(order.getPayStatus());
        if (payment != null) {
            result.setPaymentNo(payment.getPaymentNo());
            result.setPaymentStatus(payment.getStatus());
            result.setPaymentStatusText(
                    PaymentOrderStatusEnum.getDescriptionByCode(payment.getStatus()));
            result.setTransactionId(payment.getTransactionId());
            result.setAmountTotal(payment.getAmountTotal());
            result.setSuccessTime(payment.getSuccessTime());
        }
        return result;
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private String generatePaymentNo(LocalDateTime now) {
        String random = UUID.randomUUID().toString().replace("-", "");
        return "PAY" + now.format(PAYMENT_NO_TIME_FORMATTER) + random;
    }

    private String sanitizeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "payment gateway error";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private String nullIfBlank(String value) {
        return isBlank(value) ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
