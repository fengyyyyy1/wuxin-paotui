package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("home_recommendation")
public class HomeRecommendationEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("recommendation_type")
    private String recommendationType;
    @TableField("target_id")
    private Long targetId;
    @TableField("title_override")
    private String titleOverride;
    private Integer sort;
    private Integer status;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("update_admin_id")
    private Long updateAdminId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
