package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartListVO {

    private Long storeId;
    private String storeName;
    private List<CartItemVO> items;
    private BigDecimal selectedTotalAmount;
    private Long selectedProductCount;
}
