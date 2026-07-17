package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long cartId;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
    private Integer productStatus;
    private String invalidReason;
    private BigDecimal subtotal;
}
