package com.wuxin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {

    private Long categoryId;
    private String categoryName;
    private Integer sort;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
}
