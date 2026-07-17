package com.wuxin.vo;

import lombok.Data;

@Data
public class RiderRankingVO {

    private Integer rank;

    private Long riderId;

    private Long riderUserId;

    private String riderName;

    private String avatar;

    private Long completedOrderCount;
}
