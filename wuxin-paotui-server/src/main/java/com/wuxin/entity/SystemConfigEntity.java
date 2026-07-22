package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_config")
public class SystemConfigEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("config_group")
    private String configGroup;
    @TableField("config_key")
    private String configKey;
    @TableField("config_value")
    private String configValue;
    @TableField("value_type")
    private String valueType;
    @TableField("config_name")
    private String configName;
    @TableField("config_description")
    private String configDescription;
    @TableField("is_sensitive")
    private Integer sensitive;
    private Integer status;
    @TableField("update_admin_id")
    private Long updateAdminId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
