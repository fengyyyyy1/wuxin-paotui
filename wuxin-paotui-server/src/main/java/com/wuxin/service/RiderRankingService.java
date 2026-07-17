package com.wuxin.service;

import com.wuxin.vo.RiderRankingVO;
import com.wuxin.vo.RiderStatisticsVO;

import java.util.List;

public interface RiderRankingService {

    List<RiderRankingVO> getRanking(String type, Integer limit);

    RiderStatisticsVO getStatistics(Long riderId);
}
