package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentOrderVO {

    private Long commentId;

    private Long orderId;

    private Integer score;

    private LocalDateTime commentTime;
}
