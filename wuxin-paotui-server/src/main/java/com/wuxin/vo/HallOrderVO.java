package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HallOrderVO {

    private Long id;

    private String orderNo;

    private String goodsName;

    private String goodsDescription;

    private BigDecimal weight;

    private BigDecimal distance;

    private BigDecimal price;

    private Long pickupAddressId;

    private Long deliveryAddressId;

    private Integer status;

    private String statusText;

    private LocalDateTime createTime;
}
