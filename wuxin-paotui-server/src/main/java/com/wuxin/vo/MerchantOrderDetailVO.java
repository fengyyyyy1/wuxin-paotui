package com.wuxin.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantOrderDetailVO extends MerchantOrderPageVO {

    private String remark;

    private LocalDateTime merchantRejectTime;

    private String merchantRejectReason;

    private List<OrderItemVO> items;

    private List<MerchantOrderTimelineVO> timeline;
}
