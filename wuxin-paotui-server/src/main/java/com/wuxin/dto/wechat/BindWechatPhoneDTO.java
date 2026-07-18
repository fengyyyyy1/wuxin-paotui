package com.wuxin.dto.wechat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindWechatPhoneDTO {

    @NotBlank(message = "微信手机号授权凭证不能为空")
    @Size(max = 128, message = "微信手机号授权凭证长度不能超过128个字符")
    private String code;
}
