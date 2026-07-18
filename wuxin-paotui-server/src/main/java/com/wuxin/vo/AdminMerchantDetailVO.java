package com.wuxin.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminMerchantDetailVO extends AdminMerchantPageVO {

    private String username;
    private String nickname;
    private String avatar;
    private String userPhone;
    private Integer userStatus;
    private String businessLicense;
    private String idCardFront;
    private String idCardBack;
    private Long auditAdminId;
    private String auditAdminUsername;
    private String auditRemark;
    private String rejectReason;
    private String storeLogo;
    private String storeDescription;
    private String storePhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDateTime updateTime;
}
