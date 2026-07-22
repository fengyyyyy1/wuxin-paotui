package com.wuxin.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminConsoleDTO {

    private AdminConsoleDTO() {
    }

    @Data
    public static class PageQuery {
        @Min(1)
        private long pageNum = 1;
        @Min(1)
        @Max(100)
        private long pageSize = 20;
        @Size(max = 100)
        private String keyword;
    }

    @Data
    public static class OrderQuery extends PageQuery {
        private Integer orderType;
        private Integer status;
        private Long userId;
        private Long riderId;
        private Long merchantId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean abnormalOnly;
    }

    @Data
    public static class UserQuery extends PageQuery {
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Data
    public static class RiderQuery extends PageQuery {
        private Integer auditStatus;
        private Integer riderStatus;
    }

    @Data
    public static class ProductQuery extends PageQuery {
        private Long storeId;
        private Long categoryId;
        private Integer productStatus;
        private Boolean recommended;
        private Boolean hot;
    }

    @Data
    public static class OperationReason {
        @NotBlank
        @Size(min = 2, max = 255)
        private String reason;
    }

    @Data
    public static class StatusUpdate {
        @NotNull
        @Min(0)
        @Max(1)
        private Integer status;
    }

    @Data
    public static class ProductFlags {
        @NotNull
        private Boolean recommended;
        @NotNull
        private Boolean hot;
    }

    @Data
    public static class ConfigUpdate {
        @Size(max = 20000)
        private String configValue;
        @NotNull
        @Min(0)
        @Max(1)
        private Integer status;
    }

    @Data
    public static class BannerSave {
        @NotBlank
        @Size(max = 100)
        private String title;
        @Size(max = 200)
        private String subtitle;
        @NotBlank
        @Size(max = 500)
        private String imageUrl;
        @NotBlank
        @Size(max = 30)
        private String targetType;
        @Size(max = 500)
        private String targetValue;
        @NotNull
        private Integer sort;
        @NotNull
        @Min(0)
        @Max(1)
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Data
    public static class NoticeSave {
        @NotBlank
        @Size(max = 30)
        private String noticeType;
        @NotBlank
        @Size(max = 120)
        private String title;
        @NotBlank
        @Size(max = 20000)
        private String content;
        @NotNull
        @Min(0)
        @Max(2)
        private Integer status;
        private LocalDateTime publishTime;
        private LocalDateTime expireTime;
    }

    @Data
    public static class RecommendationSave {
        @NotBlank
        @Size(max = 30)
        private String recommendationType;
        @NotNull
        @Min(1)
        private Long targetId;
        @Size(max = 100)
        private String titleOverride;
        @NotNull
        private Integer sort;
        @NotNull
        @Min(0)
        @Max(1)
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Data
    public static class UserRolesUpdate {
        @NotEmpty
        private List<@NotNull Long> roleIds;
    }

    @Data
    public static class RolePermissionsUpdate {
        @NotEmpty
        private List<@NotNull Long> permissionIds;
    }

    @Data
    public static class LogQuery extends PageQuery {
        private Long adminUserId;
        @Size(max = 40)
        private String moduleCode;
        private Integer resultStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
