package com.wuxin.dto.merchant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductDTO {

    @NotNull(message = "商品分类ID不能为空")
    @Positive(message = "商品分类ID必须大于0")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100字")
    private String productName;

    @Size(max = 255, message = "商品图片地址不能超过255字")
    private String productImage;

    @Size(max = 500, message = "商品介绍不能超过500字")
    private String productDescription;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;

    private Integer sort;
}
