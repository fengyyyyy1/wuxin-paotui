package com.wuxin.dto.wechat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WeChatLoginDTO {

    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
