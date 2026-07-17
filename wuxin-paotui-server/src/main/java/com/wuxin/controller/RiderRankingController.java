package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.RiderRankingService;
import com.wuxin.vo.RiderRankingVO;
import com.wuxin.vo.RiderStatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rider")
public class RiderRankingController {

    private final RiderRankingService riderRankingService;

    public RiderRankingController(RiderRankingService riderRankingService) {
        this.riderRankingService = riderRankingService;
    }

    @GetMapping("/ranking")
    public Result<List<RiderRankingVO>> ranking(
            @RequestParam(value = "type", required = false, defaultValue = "today") String type,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return Result.success(riderRankingService.getRanking(type, limit));
    }

    @GetMapping("/{riderId}/statistics")
    public Result<RiderStatisticsVO> statistics(@PathVariable Long riderId) {
        return Result.success(riderRankingService.getStatistics(riderId));
    }
}
