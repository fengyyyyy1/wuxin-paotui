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
@TableName("order_info")
public class OrderEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("rider_id")
    private Long riderId;

    @TableField("pickup_address_id")
    private Long pickupAddressId;

    @TableField("delivery_address_id")
    private Long deliveryAddressId;

    @TableField("goods_name")
    private String goodsName;

    @TableField("goods_description")
    private String goodsDescription;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("distance")
    private BigDecimal distance;

    @TableField("price")
    private BigDecimal price;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("accept_time")
    private LocalDateTime acceptTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
