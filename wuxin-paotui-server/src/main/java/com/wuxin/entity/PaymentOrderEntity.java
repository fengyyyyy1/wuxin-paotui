package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_order")
public class PaymentOrderEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("payment_channel")
    private String paymentChannel;

    @TableField("trade_type")
    private String tradeType;

    @TableField("appid")
    private String appId;

    @TableField("mchid")
    private String mchId;

    @TableField("openid")
    private String openId;

    @TableField("amount_total")
    private Integer amountTotal;

    @TableField("currency")
    private String currency;

    @TableField("status")
    private Integer status;

    @TableField("prepay_id")
    private String prepayId;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("payer_total")
    private Integer payerTotal;

    @TableField("success_time")
    private LocalDateTime successTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("notify_id")
    private String notifyId;

    @TableField("notify_body_hash")
    private String notifyBodyHash;

    @TableField("error_code")
    private String errorCode;

    @TableField("error_message")
    private String errorMessage;

    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(
            value = "active_order_id",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Long activeOrderId;
}
