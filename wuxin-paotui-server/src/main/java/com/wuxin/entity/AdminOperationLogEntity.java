package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class AdminOperationLogEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("admin_user_id")
    private Long adminUserId;
    @TableField("module_code")
    private String moduleCode;
    @TableField("operation_code")
    private String operationCode;
    @TableField("operation_name")
    private String operationName;
    @TableField("target_type")
    private String targetType;
    @TableField("target_id")
    private String targetId;
    @TableField("request_method")
    private String requestMethod;
    @TableField("request_path")
    private String requestPath;
    @TableField("request_ip")
    private String requestIp;
    @TableField("before_data")
    private String beforeData;
    @TableField("after_data")
    private String afterData;
    @TableField("result_status")
    private Integer resultStatus;
    @TableField("error_message")
    private String errorMessage;
    @TableField("create_time")
    private LocalDateTime createTime;
}
