package com.wuxin.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectMerchantOrderDTO {

    @NotBlank(message = "拒单原因不能为空")
    @Size(min = 2, max = 200, message = "拒单原因长度必须为2到200个字符")
    private String reason;
}
