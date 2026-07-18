package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMerchantOperationVO {

    private Long merchantId;
    private Integer auditStatus;
    private String auditStatusText;
    private Integer merchantStatus;
    private String merchantStatusText;
    private Integer storeStatus;
    private String storeStatusText;
    private Integer businessStatus;
    private String businessStatusText;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private String rejectReason;
    private LocalDateTime operationTime;
}
