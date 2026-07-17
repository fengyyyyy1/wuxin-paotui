package com.wuxin.service.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentSuccessCommand {

    private String paymentNo;

    private String transactionId;

    private Integer payerTotal;

    private LocalDateTime successTime;

    private String notifyId;

    private String notifyBodyHash;
}
