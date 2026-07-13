package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfirmOrderVO {

    private Long orderId;

    private Integer status;

    private String statusText;

    private LocalDateTime confirmTime;
}
