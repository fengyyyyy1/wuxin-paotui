package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MerchantDetailVO {

    private Long merchantId;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private String businessLicense;
    private Integer auditStatus;
    private String auditStatusText;
    private String auditRemark;
    private Integer merchantStatus;
    private String merchantStatusText;
    private Long storeId;
    private String storeName;
    private String storeLogo;
    private String storeDescription;
    private String storePhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer businessStatus;
    private String businessStatusText;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer storeStatus;
    private String storeStatusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
