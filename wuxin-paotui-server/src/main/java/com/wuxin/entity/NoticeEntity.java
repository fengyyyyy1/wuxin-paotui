package com.wuxin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class NoticeEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("notice_type")
    private String noticeType;
    private String title;
    private String content;
    private Integer status;
    @TableField("publish_time")
    private LocalDateTime publishTime;
    @TableField("expire_time")
    private LocalDateTime expireTime;
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
