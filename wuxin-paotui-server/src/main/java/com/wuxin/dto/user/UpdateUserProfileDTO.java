package com.wuxin.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileDTO {

    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatar;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer gender;
}
