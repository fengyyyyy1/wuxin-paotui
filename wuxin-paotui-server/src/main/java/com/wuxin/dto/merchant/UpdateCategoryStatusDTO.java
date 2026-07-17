package com.wuxin.dto.merchant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCategoryStatusDTO {

    @NotNull(message = "商品分类状态不能为空")
    @Min(value = 0, message = "商品分类状态只能是0或1")
    @Max(value = 1, message = "商品分类状态只能是0或1")
    private Integer status;
}
