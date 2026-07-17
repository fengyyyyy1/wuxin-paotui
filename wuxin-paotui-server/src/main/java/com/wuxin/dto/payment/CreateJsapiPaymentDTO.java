package com.wuxin.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateJsapiPaymentDTO {

    @NotNull(message = "订单ID不能为空")
    @Positive(message = "订单ID必须大于0")
    private Long orderId;
}
