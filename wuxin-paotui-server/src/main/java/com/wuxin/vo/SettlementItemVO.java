package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettlementItemVO {

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    private Integer stock;
}
