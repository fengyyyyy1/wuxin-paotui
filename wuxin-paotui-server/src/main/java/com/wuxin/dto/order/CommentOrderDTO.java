package com.wuxin.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentOrderDTO {

    @NotNull(message = "订单ID不能为空")
    @Positive(message = "订单ID必须大于0")
    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在1到5之间")
    @Max(value = 5, message = "评分必须在1到5之间")
    private Integer score;

    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    @Min(value = 0, message = "匿名标识只能是0或1")
    @Max(value = 1, message = "匿名标识只能是0或1")
    private Integer anonymous;
}
