package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListVO {

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

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
