package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_audit_log")
public class MerchantAuditLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("admin_user_id")
    private Long adminUserId;

    @TableField("action")
    private String action;

    @TableField("before_status")
    private Integer beforeStatus;

    @TableField("after_status")
    private Integer afterStatus;

    @TableField("reason")
    private String reason;

    @TableField("create_time")
    private LocalDateTime createTime;
}
