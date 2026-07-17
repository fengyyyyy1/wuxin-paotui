package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {

    private Long id;

    private String orderNo;

    private Long pickupAddressId;

    private Long deliveryAddressId;

    private String goodsName;

    private String goodsDescription;

    private BigDecimal weight;

    private BigDecimal distance;

    private BigDecimal price;

    private Integer status;

    private String statusText;

    private Integer payStatus;

    private String payStatusText;

    private LocalDateTime payTime;

    private String paymentNo;

    private Integer orderType;

    private String orderTypeText;

    private Long storeId;

    private String storeName;

    private BigDecimal productAmount;

    private BigDecimal deliveryFee;

    private BigDecimal totalAmount;

    private List<OrderItemVO> items;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
