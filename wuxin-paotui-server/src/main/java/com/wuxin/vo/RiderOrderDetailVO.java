package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RiderOrderDetailVO {

    private Long id;
    private String orderNo;
    private Integer orderType;
    private String orderTypeText;
    private Long storeId;
    private String storeName;
    private String goodsName;
    private String goodsDescription;
    private String goodsSummary;
    private List<OrderItemVO> items;
    private BigDecimal weight;
    private BigDecimal distance;
    private BigDecimal price;
    private BigDecimal productAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private Integer payStatus;
    private String payStatusText;
    private String remark;
    private String pickupName;
    private String pickupPhone;
    private String pickupAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private String deliveryName;
    private String deliveryPhone;
    private String deliveryAddress;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    private List<RiderOrderTimelineVO> timeline;
}
