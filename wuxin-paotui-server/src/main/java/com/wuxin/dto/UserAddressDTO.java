package com.wuxin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAddressDTO {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 30, message = "收货人不能超过30个字符")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String receiverPhone;

    @Size(max = 30, message = "省份不能超过30个字符")
    private String province;

    @Size(max = 30, message = "城市不能超过30个字符")
    private String city;

    @Size(max = 30, message = "区县不能超过30个字符")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 120, message = "详细地址不能超过120个字符")
    private String detailAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer isDefault;
}
