package com.wuxin.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminConsoleVO {

    private AdminConsoleVO() {
    }

    @Data
    public static class Session {
        private Long userId;
        private String username;
        private String nickname;
        private List<String> roles;
        private List<String> permissions;
    }

    @Data
    public static class Dashboard {
        private long todayOrders;
        private BigDecimal todayRevenue;
        private long todayDeliveries;
        private long newUsers;
        private long newRiders;
        private long newMerchants;
        private long pendingRiders;
        private long pendingMerchants;
        private long pendingRefunds;
        private List<TrendPoint> orderTrend;
        private List<TrendPoint> revenueTrend;
        private List<RankingItem> topProducts;
        private List<RankingItem> topMerchants;
        private List<RankingItem> topRiders;
        private List<OrderRow> recentOrders;
        private List<Notice> notices;
    }

    @Data
    public static class TrendPoint {
        private LocalDate date;
        private BigDecimal value;
    }

    @Data
    public static class RankingItem {
        private Long id;
        private String name;
        private BigDecimal value;
        private Long count;
    }

    @Data
    public static class OrderRow {
        private Long orderId;
        private String orderNo;
        private Integer orderType;
        private String orderTypeText;
        private Long userId;
        private String userName;
        private Long riderId;
        private String riderName;
        private Long merchantId;
        private Long storeId;
        private String storeName;
        private String goodsName;
        private BigDecimal totalAmount;
        private Integer payStatus;
        private Integer status;
        private String statusText;
        private LocalDateTime createTime;
        private LocalDateTime finishTime;
        private boolean abnormal;
    }

    @Data
    public static class OrderDetail extends OrderRow {
        private BigDecimal productAmount;
        private BigDecimal deliveryFee;
        private BigDecimal weight;
        private BigDecimal distance;
        private String remark;
        private LocalDateTime payTime;
        private List<OrderItem> items;
        private List<OrderLog> logs;
    }

    @Data
    public static class OrderItem {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    public static class OrderLog {
        private Long id;
        private Integer oldStatus;
        private Integer newStatus;
        private Long operatorId;
        private String operatorType;
        private String remark;
        private LocalDateTime createTime;
    }

    @Data
    public static class UserRow {
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer status;
        private long orderCount;
        private BigDecimal consumptionAmount;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;
    }

    @Data
    public static class RiderRow {
        private Long riderId;
        private Long userId;
        private String username;
        private String nickname;
        private String phone;
        private String realName;
        private String idCard;
        private String idCardFront;
        private String idCardBack;
        private Integer auditStatus;
        private String auditStatusText;
        private Integer riderStatus;
        private String riderStatusText;
        private String rejectReason;
        private long deliveryCount;
        private long completedCount;
        private BigDecimal completionRate;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    @Data
    public static class ProductRow {
        private Long productId;
        private Long storeId;
        private String storeName;
        private Long merchantId;
        private Long categoryId;
        private String categoryName;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer stock;
        private Integer sales;
        private Integer productStatus;
        private boolean recommended;
        private boolean hot;
        private LocalDateTime updateTime;
    }

    @Data
    public static class CategoryRow {
        private Long categoryId;
        private Long storeId;
        private String storeName;
        private String categoryName;
        private Integer status;
        private Integer sort;
        private long productCount;
    }

    @Data
    public static class Finance {
        private BigDecimal platformRevenue;
        private BigDecimal todayIncome;
        private BigDecimal yesterdayIncome;
        private BigDecimal monthIncome;
        private BigDecimal orderAmount;
        private BigDecimal platformCommission;
        private BigDecimal merchantIncome;
        private BigDecimal riderIncome;
        private BigDecimal platformCommissionRate;
        private BigDecimal merchantCommissionRate;
        private BigDecimal riderRewardRate;
    }

    @Data
    public static class Config {
        private Long id;
        private String configGroup;
        private String configKey;
        private String configValue;
        private String valueType;
        private String configName;
        private String configDescription;
        private Integer sensitive;
        private Integer status;
        private Long updateAdminId;
        private LocalDateTime updateTime;
    }

    @Data
    public static class Banner {
        private Long id;
        private String title;
        private String subtitle;
        private String imageUrl;
        private String targetType;
        private String targetValue;
        private Integer sort;
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime updateTime;
    }

    @Data
    public static class Notice {
        private Long id;
        private String noticeType;
        private String title;
        private String content;
        private Integer status;
        private LocalDateTime publishTime;
        private LocalDateTime expireTime;
        private LocalDateTime updateTime;
    }

    @Data
    public static class Recommendation {
        private Long id;
        private String recommendationType;
        private Long targetId;
        private String targetName;
        private String titleOverride;
        private Integer sort;
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime updateTime;
    }

    @Data
    public static class Role {
        private Long roleId;
        private String roleName;
        private String roleCode;
        private String roleDescription;
        private Integer status;
        private List<Long> permissionIds;
    }

    @Data
    public static class Permission {
        private Long permissionId;
        private String permissionName;
        private String permissionCode;
        private String moduleCode;
        private String permissionType;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class AdminUser {
        private Long userId;
        private String username;
        private String nickname;
        private String phone;
        private Integer status;
        private LocalDateTime lastLoginTime;
        private List<Long> roleIds;
        private List<String> roleNames;
    }

    @Data
    public static class OperationLog {
        private Long id;
        private Long adminUserId;
        private String adminUsername;
        private String moduleCode;
        private String operationCode;
        private String operationName;
        private String targetType;
        private String targetId;
        private String requestMethod;
        private String requestPath;
        private String requestIp;
        private String beforeData;
        private String afterData;
        private Integer resultStatus;
        private String errorMessage;
        private LocalDateTime createTime;
    }

    @Data
    public static class PublicHome {
        private List<Banner> banners;
        private List<Notice> notices;
        private List<Recommendation> recommendations;
        private List<Config> configs;
    }
}
