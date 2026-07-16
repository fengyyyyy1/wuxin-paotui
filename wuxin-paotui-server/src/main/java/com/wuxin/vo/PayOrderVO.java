package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayOrderVO {

    private Long orderId;

    private String paymentNo;

    private Integer payStatus;

    private String payStatusText;

    private BigDecimal amount;

    private LocalDateTime payTime;
}
