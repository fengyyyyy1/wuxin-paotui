package com.wuxin.dto.merchant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class UpdateMerchantStoreDTO {

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称不能超过100字")
    private String storeName;

    @Size(max = 255, message = "店铺Logo地址不能超过255字")
    private String storeLogo;

    @Size(max = 500, message = "店铺简介不能超过500字")
    private String storeDescription;

    @NotBlank(message = "店铺联系电话不能为空")
    @Size(max = 20, message = "店铺联系电话不能超过20字")
    private String storePhone;

    @Size(max = 50, message = "省份不能超过50字")
    private String province;

    @Size(max = 50, message = "城市不能超过50字")
    private String city;

    @Size(max = 50, message = "区县不能超过50字")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255字")
    private String detailAddress;

    @DecimalMin(value = "-90", message = "纬度不能小于-90")
    @DecimalMax(value = "90", message = "纬度不能大于90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "经度不能小于-180")
    @DecimalMax(value = "180", message = "经度不能大于180")
    private BigDecimal longitude;

    private LocalTime openTime;

    private LocalTime closeTime;
}
