package com.wuxin.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartDTO {

    @NotNull(message = "购物车ID不能为空")
    @Positive(message = "购物车ID必须大于0")
    private Long cartId;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;
}
