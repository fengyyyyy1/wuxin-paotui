package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantOrderTimelineVO {

    private Integer oldStatus;

    private String oldStatusName;

    private Integer newStatus;

    private String newStatusName;

    private String operatorType;

    private String remark;

    private LocalDateTime createTime;
}
