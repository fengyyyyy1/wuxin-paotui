package com.wuxin.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderDTO {

    @NotNull(message = "pickupAddressId cannot be null")
    private Long pickupAddressId;

    @NotNull(message = "deliveryAddressId cannot be null")
    private Long deliveryAddressId;

    @NotBlank(message = "goodsName cannot be blank")
    @Size(max = 100, message = "goodsName length cannot exceed 100")
    private String goodsName;

    @Size(max = 500, message = "goodsDescription length cannot exceed 500")
    private String goodsDescription;

    @NotNull(message = "weight cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "distance cannot be null")
    @DecimalMin(value = "0", message = "distance must be greater than or equal to 0")
    private BigDecimal distance;

    @NotNull(message = "price cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "price must be greater than 0")
    private BigDecimal price;

    @Size(max = 500, message = "remark length cannot exceed 500")
    private String remark;
}
