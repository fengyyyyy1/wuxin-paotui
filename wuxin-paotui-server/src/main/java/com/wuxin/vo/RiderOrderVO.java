package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiderOrderVO {

    private Long id;

    private String orderNo;

    private String goodsName;

    private String goodsDescription;

    private BigDecimal weight;

    private BigDecimal distance;

    private BigDecimal price;

    private Long pickupAddressId;

    private Long deliveryAddressId;

    private Integer orderType;

    private String orderTypeText;

    private Long storeId;

    private String storeName;

    private String pickupAddress;

    private String deliveryAddress;

    private String goodsSummary;

    private Integer status;

    private String statusText;

    private Integer payStatus;

    private String payStatusText;

    private LocalDateTime acceptTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;
}
