package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SettlementPreviewVO {

    private Long storeId;

    private String storeName;

    private Long deliveryAddressId;

    private List<SettlementItemVO> items;

    private BigDecimal productAmount;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private Long selectedProductCount;
}
