package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long productId;
    private Long storeId;
    private Long categoryId;
    private String categoryName;
    private String productName;
    private String productImage;
    private String productDescription;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer productStatus;
    private String productStatusText;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
