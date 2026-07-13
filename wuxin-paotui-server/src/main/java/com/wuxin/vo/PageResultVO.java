package com.wuxin.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResultVO<T> {

    private List<T> records;

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private Long pages;
}
