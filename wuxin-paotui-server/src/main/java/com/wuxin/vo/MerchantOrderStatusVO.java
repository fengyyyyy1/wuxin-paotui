package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantOrderStatusVO {

    private Long orderId;

    private Integer status;

    private String statusName;

    private LocalDateTime merchantAcceptTime;

    private LocalDateTime readyTime;

    private LocalDateTime rejectTime;

    private String rejectReason;
}
