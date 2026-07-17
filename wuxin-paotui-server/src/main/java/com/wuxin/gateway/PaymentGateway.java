package com.wuxin.gateway;

import com.wuxin.gateway.model.PaymentGatewayCreateRequest;
import com.wuxin.gateway.model.PaymentGatewayCreateResult;
import com.wuxin.gateway.model.PaymentGatewayQueryResult;

public interface PaymentGateway {

    String getType();

    PaymentGatewayCreateResult createPayment(PaymentGatewayCreateRequest request);

    PaymentGatewayCreateResult buildPaymentParameters(
            PaymentGatewayCreateRequest request, String prepayId);

    PaymentGatewayQueryResult queryPayment(String paymentNo);
}
