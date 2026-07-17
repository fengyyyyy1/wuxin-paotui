package com.wuxin.gateway.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayCreateRequest {

    private String paymentNo;

    private String orderNo;

    private String description;

    private Integer amountTotal;

    private String currency;

    private String appId;

    private String mchId;

    private String openId;

    private String notifyUrl;
}
