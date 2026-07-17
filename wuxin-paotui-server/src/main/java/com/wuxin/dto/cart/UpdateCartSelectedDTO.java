package com.wuxin.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartSelectedDTO {

    @NotNull(message = "购物车ID不能为空")
    @Positive(message = "购物车ID必须大于0")
    private Long cartId;

    @NotNull(message = "选中状态不能为空")
    @Min(value = 0, message = "选中状态只能是0或1")
    @Max(value = 1, message = "选中状态只能是0或1")
    private Integer selected;
}
