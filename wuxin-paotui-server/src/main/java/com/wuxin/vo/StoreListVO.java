package com.wuxin.vo;

import lombok.Data;

import java.time.LocalTime;

@Data
public class StoreListVO {

    private Long storeId;
    private Long merchantId;
    private String storeName;
    private String storeLogo;
    private String storeDescription;
    private String storePhone;
    private String district;
    private String detailAddress;
    private Integer businessStatus;
    private String businessStatusText;
    private LocalTime openTime;
    private LocalTime closeTime;
}
