package com.wuxin.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryDTO {

    @NotBlank(message = "商品分类名称不能为空")
    @Size(max = 50, message = "商品分类名称不能超过50字")
    private String categoryName;

    private Integer sort;
}
