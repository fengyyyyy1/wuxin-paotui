package com.wuxin.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderTimelineVO {

    private Long orderId;

    private String orderNo;

    private Integer status;

    private String statusText;

    private Integer payStatus;

    private String payStatusText;

    private List<OrderTimelineItemVO> timeline;
}
