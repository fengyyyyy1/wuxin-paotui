package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_product")
public class MerchantProductEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("store_id")
    private Long storeId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("product_name")
    private String productName;

    @TableField("product_image")
    private String productImage;

    @TableField("product_description")
    private String productDescription;

    @TableField("price")
    private BigDecimal price;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("sales")
    private Integer sales;

    @TableField("product_status")
    private Integer productStatus;

    @TableField("sort")
    private Integer sort;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
