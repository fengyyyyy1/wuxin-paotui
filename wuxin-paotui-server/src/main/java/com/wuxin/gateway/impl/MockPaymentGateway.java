package com.wuxin.gateway.impl;

import com.wuxin.enums.PaymentOrderStatusEnum;
import com.wuxin.gateway.PaymentGateway;
import com.wuxin.gateway.PaymentGatewayRouter;
import com.wuxin.gateway.model.PaymentGatewayCreateRequest;
import com.wuxin.gateway.model.PaymentGatewayCreateResult;
import com.wuxin.gateway.model.PaymentGatewayQueryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "wuxin.mock-payment",
        name = "enabled",
        havingValue = "true")
public class MockPaymentGateway implements PaymentGateway {

    private static final String SIGN_TYPE = "RSA";

    @Override
    public String getType() {
        return PaymentGatewayRouter.MOCK_GATEWAY;
    }

    @Override
    public PaymentGatewayCreateResult createPayment(PaymentGatewayCreateRequest request) {
        // Local mock intentionally accepts a null openid; a real JSAPI gateway must reject it.
        String prepayId = "mock_prepay_" + compactUuid();
        return buildPaymentParameters(request, prepayId);
    }

    @Override
    public PaymentGatewayCreateResult buildPaymentParameters(
            PaymentGatewayCreateRequest request, String prepayId) {
        String nonce = compactUuid();
        return PaymentGatewayCreateResult.builder()
                .prepayId(prepayId)
                .timeStamp(String.valueOf(Instant.now().getEpochSecond()))
                .nonceStr(nonce)
                .packageValue("prepay_id=" + prepayId)
                .signType(SIGN_TYPE)
                .paySign("MOCK_SIGN_" + nonce)
                .build();
    }

    @Override
    public PaymentGatewayQueryResult queryPayment(String paymentNo) {
        return PaymentGatewayQueryResult.builder()
                .paymentNo(paymentNo)
                .status(PaymentOrderStatusEnum.WAITING_PAY.getCode())
                .build();
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
