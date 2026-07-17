package com.wuxin.vo;

import lombok.Data;

@Data
public class RiderStatisticsVO {

    private Long riderId;

    private Long todayCompletedCount;

    private Long weekCompletedCount;

    private Long monthCompletedCount;

    private Long totalCompletedCount;
}
