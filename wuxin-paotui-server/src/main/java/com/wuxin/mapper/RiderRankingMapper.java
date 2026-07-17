package com.wuxin.mapper;

import com.wuxin.vo.RiderRankingVO;
import com.wuxin.vo.RiderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiderRankingMapper {

    @Select({
            "<script>",
            "SELECT",
            "  r.id AS riderId,",
            "  r.user_id AS riderUserId,",
            "  COALESCE(NULLIF(TRIM(r.real_name), ''),",
            "           NULLIF(TRIM(u.nickname), ''),",
            "           CONCAT('骑手', r.id)) AS riderName,",
            "  u.avatar AS avatar,",
            "  COUNT(o.id) AS completedOrderCount",
            "FROM order_info o",
            "INNER JOIN rider_info r ON r.id = o.rider_id",
            "LEFT JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0",
            "WHERE o.status = #{completedStatus}",
            "  AND o.rider_id IS NOT NULL",
            "  AND o.deleted = 0",
            "<if test='startTime != null and endTime != null'>",
            "  AND o.finish_time &gt;= #{startTime}",
            "  AND o.finish_time &lt; #{endTime}",
            "</if>",
            "GROUP BY r.id, r.user_id, r.real_name, u.nickname, u.avatar",
            "ORDER BY completedOrderCount DESC,",
            "         MIN(o.finish_time) IS NULL ASC,",
            "         MIN(o.finish_time) ASC,",
            "         r.id ASC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<RiderRankingVO> selectRanking(
            @Param("completedStatus") Integer completedStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    @Select({
            "SELECT",
            "  r.id AS riderId,",
            "  COUNT(CASE WHEN o.finish_time >= #{todayStart}",
            "                  AND o.finish_time < #{todayEnd} THEN 1 END) AS todayCompletedCount,",
            "  COUNT(CASE WHEN o.finish_time >= #{weekStart}",
            "                  AND o.finish_time < #{weekEnd} THEN 1 END) AS weekCompletedCount,",
            "  COUNT(CASE WHEN o.finish_time >= #{monthStart}",
            "                  AND o.finish_time < #{monthEnd} THEN 1 END) AS monthCompletedCount,",
            "  COUNT(o.id) AS totalCompletedCount",
            "FROM rider_info r",
            "LEFT JOIN order_info o",
            "  ON o.rider_id = r.id",
            " AND o.status = #{completedStatus}",
            " AND o.deleted = 0",
            "WHERE r.id = #{riderId}",
            "GROUP BY r.id"
    })
    RiderStatisticsVO selectStatistics(
            @Param("riderId") Long riderId,
            @Param("completedStatus") Integer completedStatus,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayEnd") LocalDateTime todayEnd,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEnd") LocalDateTime monthEnd);
}
