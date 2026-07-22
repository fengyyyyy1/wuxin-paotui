package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("banner")
public class BannerEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private String subtitle;
    @TableField("image_url")
    private String imageUrl;
    @TableField("target_type")
    private String targetType;
    @TableField("target_value")
    private String targetValue;
    private Integer sort;
    private Integer status;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("create_admin_id")
    private Long createAdminId;
    @TableField("update_admin_id")
    private Long updateAdminId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
