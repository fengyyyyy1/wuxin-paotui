package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiderApplyVO {

    private Long riderId;
    private Integer auditStatus;
    private String auditStatusText;
    private LocalDateTime applyTime;
}
