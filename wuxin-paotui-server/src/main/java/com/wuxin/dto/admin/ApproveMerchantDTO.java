package com.wuxin.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApproveMerchantDTO {

    @NotBlank(message = "审核备注不能为空")
    @Size(max = 255, message = "审核备注长度不能超过255个字符")
    private String auditRemark;
}
