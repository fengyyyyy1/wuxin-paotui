package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCartOrderVO {

    private Long orderId;

    private String orderNo;

    private Integer orderType;

    private Long storeId;

    private BigDecimal productAmount;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private Integer payStatus;

    private Integer status;

    private Long itemCount;
}
