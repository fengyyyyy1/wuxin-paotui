package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRiderOperationVO {

    private Long riderId;
    private Integer auditStatus;
    private String auditStatusText;
    private Integer riderStatus;
    private String riderStatusText;
    private String rejectReason;
    private LocalDateTime operationTime;
}
