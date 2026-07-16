package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantApplyVO {

    private Long merchantId;

    private Long storeId;

    private Integer auditStatus;

    private String auditStatusText;

    private LocalDateTime applyTime;
}
