package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentStatusVO {

    private Long orderId;

    private String orderNo;

    private Integer payStatus;

    private String paymentNo;

    private Integer paymentStatus;

    private String paymentStatusText;

    private String transactionId;

    private Integer amountTotal;

    private LocalDateTime successTime;
}
