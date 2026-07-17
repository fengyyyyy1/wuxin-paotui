package com.wuxin.gateway.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayCreateResult {

    private String prepayId;

    private String timeStamp;

    private String nonceStr;

    private String packageValue;

    private String signType;

    private String paySign;
}
