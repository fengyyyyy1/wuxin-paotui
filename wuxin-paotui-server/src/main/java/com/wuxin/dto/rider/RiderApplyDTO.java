package com.wuxin.dto.rider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RiderApplyDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50字")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCard;

    @NotBlank(message = "身份证正面地址不能为空")
    @Size(max = 255, message = "身份证正面地址不能超过255字")
    private String idCardFront;

    @NotBlank(message = "身份证反面地址不能为空")
    @Size(max = 255, message = "身份证反面地址不能超过255字")
    private String idCardBack;
}
