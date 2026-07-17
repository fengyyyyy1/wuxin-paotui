package com.wuxin.service.impl;

import com.wuxin.common.ResultCode;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.RankingTypeEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.RiderRankingMapper;
import com.wuxin.service.RiderRankingService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.RiderRankingVO;
import com.wuxin.vo.RiderStatisticsVO;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class RiderRankingServiceImpl implements RiderRankingService {

    private static final int DEFAULT_LIMIT = 10;

    private static final int MIN_LIMIT = 1;

    private static final int MAX_LIMIT = 100;

    private final RiderRankingMapper riderRankingMapper;

    public RiderRankingServiceImpl(RiderRankingMapper riderRankingMapper) {
        this.riderRankingMapper = riderRankingMapper;
    }

    @Override
    public List<RiderRankingVO> getRanking(String type, Integer limit) {
        ensureLoggedIn();

        RankingTypeEnum rankingType = resolveRankingType(type);
        int safeLimit = resolveLimit(limit);
        TimeBoundaries boundaries = TimeBoundaries.current();
        TimeRange range = boundaries.rangeOf(rankingType);

        List<RiderRankingVO> ranking = riderRankingMapper.selectRanking(
                OrderStatusEnum.COMPLETED.getCode(),
                range.startTime(),
                range.endTime(),
                safeLimit);

        for (int index = 0; index < ranking.size(); index++) {
            ranking.get(index).setRank(index + 1);
        }
        return ranking;
    }

    @Override
    public RiderStatisticsVO getStatistics(Long riderId) {
        ensureLoggedIn();
        if (riderId == null || riderId <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        TimeBoundaries boundaries = TimeBoundaries.current();
        RiderStatisticsVO statistics = riderRankingMapper.selectStatistics(
                riderId,
                OrderStatusEnum.COMPLETED.getCode(),
                boundaries.todayStart(),
                boundaries.todayEnd(),
                boundaries.weekStart(),
                boundaries.weekEnd(),
                boundaries.monthStart(),
                boundaries.monthEnd());

        if (statistics == null) {
            throw new BusinessException(ResultCode.RIDER_NOT_EXIST);
        }
        return statistics;
    }

    private void ensureLoggedIn() {
        if (UserContext.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    private RankingTypeEnum resolveRankingType(String type) {
        String value = type == null || type.isBlank()
                ? RankingTypeEnum.TODAY.getValue()
                : type;
        RankingTypeEnum rankingType = RankingTypeEnum.of(value);
        if (rankingType == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "排行榜类型参数错误");
        }
        return rankingType;
    }

    private int resolveLimit(Integer limit) {
        int value = limit == null ? DEFAULT_LIMIT : limit;
        if (value < MIN_LIMIT || value > MAX_LIMIT) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "limit 必须在 1 到 100 之间");
        }
        return value;
    }

    private record TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    }

    private record TimeBoundaries(
            LocalDateTime todayStart,
            LocalDateTime todayEnd,
            LocalDateTime weekStart,
            LocalDateTime weekEnd,
            LocalDateTime monthStart,
            LocalDateTime monthEnd) {

        private static TimeBoundaries current() {
            LocalDate today = LocalDate.now();
            LocalDate weekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate monthFirstDay = today.withDayOfMonth(1);
            return new TimeBoundaries(
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay(),
                    weekMonday.atStartOfDay(),
                    weekMonday.plusWeeks(1).atStartOfDay(),
                    monthFirstDay.atStartOfDay(),
                    monthFirstDay.plusMonths(1).atStartOfDay());
        }

        private TimeRange rangeOf(RankingTypeEnum type) {
            return switch (type) {
                case TODAY -> new TimeRange(todayStart, todayEnd);
                case WEEK -> new TimeRange(weekStart, weekEnd);
                case MONTH -> new TimeRange(monthStart, monthEnd);
                case TOTAL -> new TimeRange(null, null);
            };
        }
    }
}
