package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMerchantPageVO {

    private Long merchantId;
    private Long userId;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private Integer auditStatus;
    private String auditStatusText;
    private Integer merchantStatus;
    private String merchantStatusText;
    private Long storeId;
    private String storeName;
    private Integer storeStatus;
    private String storeStatusText;
    private Integer businessStatus;
    private String businessStatusText;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
}
