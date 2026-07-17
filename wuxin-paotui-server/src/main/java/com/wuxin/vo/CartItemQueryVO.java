package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemQueryVO {

    private Long cartId;
    private Long storeId;
    private Long productStoreId;
    private String storeName;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
    private Integer productStatus;
    private Integer productExists;
    private Integer productDeleted;
    private Integer categoryExists;
    private Integer categoryStatus;
    private Integer categoryDeleted;
    private Integer storeExists;
    private Integer storeStatus;
    private Integer businessStatus;
    private Integer storeDeleted;
    private Integer merchantExists;
    private Integer merchantAuditStatus;
    private Integer merchantStatus;
    private Integer merchantDeleted;
}
