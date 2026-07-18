package com.wuxin.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminMerchantPageQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    @Min(value = 0, message = "审核状态参数错误")
    @Max(value = 2, message = "审核状态参数错误")
    private Integer auditStatus;

    @Min(value = 0, message = "商家状态参数错误")
    @Max(value = 1, message = "商家状态参数错误")
    private Integer merchantStatus;

    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;
}
