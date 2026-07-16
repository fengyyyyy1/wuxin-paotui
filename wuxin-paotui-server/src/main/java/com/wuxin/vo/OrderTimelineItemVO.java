package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTimelineItemVO {

    private String type;

    private String title;

    private String description;

    private LocalDateTime time;

    private Integer sort;
}
