package com.wuxin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class StoreDetailVO {

    private Long storeId;
    private Long merchantId;
    private String merchantName;
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
}
