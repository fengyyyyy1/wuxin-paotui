package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiderOrderTimelineVO {

    private Integer oldStatus;
    private String oldStatusText;
    private Integer newStatus;
    private String newStatusText;
    private String operatorType;
    private String remark;
    private LocalDateTime createTime;
}
