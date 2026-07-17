package com.wuxin.gateway.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentGatewayQueryResult {

    private String paymentNo;

    private Integer status;

    private String transactionId;

    private Integer payerTotal;

    private LocalDateTime successTime;
}
