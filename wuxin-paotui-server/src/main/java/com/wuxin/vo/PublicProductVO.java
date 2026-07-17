package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublicProductVO {

    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String productName;
    private String productImage;
    private String productDescription;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer sort;
}
