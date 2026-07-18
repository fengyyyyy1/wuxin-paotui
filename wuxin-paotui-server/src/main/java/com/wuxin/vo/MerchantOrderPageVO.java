package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantOrderPageVO {

    private Long orderId;

    private String orderNo;

    private Integer status;

    private String statusName;

    private Integer payStatus;

    private String payStatusName;

    private BigDecimal productAmount;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private String goodsSummary;

    private String receiverName;

    private String receiverPhone;

    private String deliveryAddress;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime merchantAcceptTime;

    private LocalDateTime readyTime;
}
