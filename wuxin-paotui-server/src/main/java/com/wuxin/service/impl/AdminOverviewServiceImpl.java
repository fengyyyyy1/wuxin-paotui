package com.wuxin.service.impl;

import com.wuxin.mapper.AdminConsoleMapper;
import com.wuxin.service.AdminOperationsService;
import com.wuxin.service.AdminOverviewService;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminOverviewServiceImpl implements AdminOverviewService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final AdminConsoleMapper mapper;
    private final AdminOperationsService operationsService;

    public AdminOverviewServiceImpl(AdminConsoleMapper mapper, AdminOperationsService operationsService) {
        this.mapper = mapper;
        this.operationsService = operationsService;
    }

    @Override
    public AdminConsoleVO.Dashboard dashboard() {
        AdminConsoleVO.Dashboard result = mapper.selectDashboardSummary();
        result.setTodayRevenue(money(result.getTodayRevenue()));
        result.setOrderTrend(fillTrend(mapper.selectOrderTrend()));
        result.setRevenueTrend(fillTrend(mapper.selectRevenueTrend()));
        result.setTopProducts(mapper.selectTopProducts());
        result.setTopMerchants(mapper.selectTopMerchants());
        result.setTopRiders(mapper.selectTopRiders());
        List<AdminConsoleVO.OrderRow> orders = mapper.selectRecentOrders();
        orders.forEach(this::enrichOrder);
        result.setRecentOrders(orders);
        result.setNotices(operationsService.getPublicHome().getNotices().stream().limit(8).toList());
        return result;
    }

    @Override
    public AdminConsoleVO.Finance finance() {
        AdminConsoleVO.Finance result = mapper.selectFinanceBase();
        BigDecimal platformRate = operationsService.getDecimal("platform.commission_rate", ZERO);
        BigDecimal merchantRate = operationsService.getDecimal("platform.merchant_commission_rate", ZERO);
        BigDecimal riderRate = operationsService.getDecimal("platform.rider_reward_rate", ZERO);
        result.setPlatformCommissionRate(platformRate);
        result.setMerchantCommissionRate(merchantRate);
        result.setRiderRewardRate(riderRate);
        result.setOrderAmount(money(result.getOrderAmount()));
        result.setTodayIncome(money(result.getTodayIncome()));
        result.setYesterdayIncome(money(result.getYesterdayIncome()));
        result.setMonthIncome(money(result.getMonthIncome()));
        result.setPlatformCommission(money(result.getOrderAmount().multiply(platformRate)));
        result.setPlatformRevenue(result.getPlatformCommission());
        result.setMerchantIncome(money(result.getOrderAmount().multiply(BigDecimal.ONE.subtract(merchantRate))));
        result.setRiderIncome(money(result.getOrderAmount().multiply(riderRate)));
        return result;
    }

    private List<AdminConsoleVO.TrendPoint> fillTrend(List<AdminConsoleVO.TrendPoint> source) {
        Map<LocalDate, AdminConsoleVO.TrendPoint> indexed = source.stream()
                .collect(Collectors.toMap(AdminConsoleVO.TrendPoint::getDate, Function.identity()));
        List<AdminConsoleVO.TrendPoint> result = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = LocalDate.now().minusDays(offset);
            AdminConsoleVO.TrendPoint point = indexed.get(date);
            if (point == null) {
                point = new AdminConsoleVO.TrendPoint();
                point.setDate(date);
                point.setValue(ZERO);
            }
            result.add(point);
        }
        return result;
    }

    private void enrichOrder(AdminConsoleVO.OrderRow order) {
        order.setOrderTypeText(Integer.valueOf(1).equals(order.getOrderType()) ? "商品订单" : "跑腿订单");
        order.setStatusText(com.wuxin.enums.OrderStatusEnum.getDescriptionByCode(
                order.getStatus(), order.getOrderType(), order.getPayStatus()));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
