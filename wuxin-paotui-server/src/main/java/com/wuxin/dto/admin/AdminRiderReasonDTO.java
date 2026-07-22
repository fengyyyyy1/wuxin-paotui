package com.wuxin.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRiderReasonDTO {

    @NotBlank(message = "原因不能为空")
    @Size(min = 2, max = 255, message = "原因长度必须为2到255个字符")
    private String reason;
}
