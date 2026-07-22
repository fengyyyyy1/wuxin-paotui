package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.entity.BannerEntity;
import com.wuxin.entity.HomeRecommendationEntity;
import com.wuxin.entity.NoticeEntity;
import com.wuxin.entity.SystemConfigEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.BannerMapper;
import com.wuxin.mapper.HomeRecommendationMapper;
import com.wuxin.mapper.MerchantCategoryMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.mapper.NoticeMapper;
import com.wuxin.mapper.SystemConfigMapper;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.service.AdminOperationsService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminOperationsServiceImpl implements AdminOperationsService {

    private static final String MASKED_VALUE = "******";
    private static final Set<String> TARGET_TYPES =
            Set.of("NONE", "STORE", "PRODUCT", "PAGE", "URL");
    private static final Set<String> RECOMMENDATION_TYPES =
            Set.of("STORE", "PRODUCT", "HOT_PRODUCT", "CATEGORY");

    private final SystemConfigMapper configMapper;
    private final BannerMapper bannerMapper;
    private final NoticeMapper noticeMapper;
    private final HomeRecommendationMapper recommendationMapper;
    private final MerchantStoreMapper storeMapper;
    private final MerchantProductMapper productMapper;
    private final MerchantCategoryMapper categoryMapper;
    private final AdminAuditLogService auditLogService;

    public AdminOperationsServiceImpl(
            SystemConfigMapper configMapper,
            BannerMapper bannerMapper,
            NoticeMapper noticeMapper,
            HomeRecommendationMapper recommendationMapper,
            MerchantStoreMapper storeMapper,
            MerchantProductMapper productMapper,
            MerchantCategoryMapper categoryMapper,
            AdminAuditLogService auditLogService) {
        this.configMapper = configMapper;
        this.bannerMapper = bannerMapper;
        this.noticeMapper = noticeMapper;
        this.recommendationMapper = recommendationMapper;
        this.storeMapper = storeMapper;
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<AdminConsoleVO.Config> listConfigs(String group) {
        LambdaQueryWrapper<SystemConfigEntity> query = new LambdaQueryWrapper<>();
        query.eq(group != null && !group.isBlank(), SystemConfigEntity::getConfigGroup, group)
                .orderByAsc(SystemConfigEntity::getConfigGroup)
                .orderByAsc(SystemConfigEntity::getId);
        return configMapper.selectList(query).stream().map(this::toConfig).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.Config updateConfig(Long id, AdminConsoleDTO.ConfigUpdate request) {
        SystemConfigEntity config = requireConfig(id);
        String nextValue = normalizeConfigValue(config, request.getConfigValue());
        SystemConfigEntity before = copyConfig(config);
        config.setConfigValue(nextValue);
        config.setStatus(request.getStatus());
        config.setUpdateAdminId(currentUserId());
        config.setUpdateTime(LocalDateTime.now());
        if (configMapper.updateById(config) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置更新失败");
        }
        auditLogService.record("config", "config:update", "修改系统配置", "SYSTEM_CONFIG", id, before, config);
        return toConfig(config);
    }

    @Override
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        SystemConfigEntity config = configMapper.selectOne(new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, key)
                .eq(SystemConfigEntity::getStatus, 1)
                .last("LIMIT 1"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    @Override
    public List<AdminConsoleVO.Banner> listBanners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<BannerEntity>()
                        .orderByDesc(BannerEntity::getStatus)
                        .orderByAsc(BannerEntity::getSort)
                        .orderByDesc(BannerEntity::getId))
                .stream().map(this::toBanner).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.Banner saveBanner(Long id, AdminConsoleDTO.BannerSave request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        String targetType = request.getTargetType().trim().toUpperCase(Locale.ROOT);
        if (!TARGET_TYPES.contains(targetType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Banner跳转类型不合法");
        }
        BannerEntity banner = id == null ? new BannerEntity() : requireBanner(id);
        BannerEntity before = id == null ? null : copyBanner(banner);
        LocalDateTime now = LocalDateTime.now();
        banner.setTitle(request.getTitle().trim());
        banner.setSubtitle(trimToNull(request.getSubtitle()));
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setTargetType(targetType);
        banner.setTargetValue(trimToNull(request.getTargetValue()));
        banner.setSort(request.getSort());
        banner.setStatus(request.getStatus());
        banner.setStartTime(request.getStartTime());
        banner.setEndTime(request.getEndTime());
        banner.setUpdateAdminId(currentUserId());
        banner.setUpdateTime(now);
        if (id == null) {
            banner.setCreateAdminId(currentUserId());
            banner.setCreateTime(now);
            banner.setIsDeleted(0);
            bannerMapper.insert(banner);
        } else if (bannerMapper.updateById(banner) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Banner更新失败");
        }
        auditLogService.record("operation", id == null ? "banner:create" : "banner:update",
                id == null ? "新增Banner" : "修改Banner", "BANNER", banner.getId(), before, banner);
        return toBanner(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        BannerEntity banner = requireBanner(id);
        if (bannerMapper.deleteById(id) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Banner删除失败");
        }
        auditLogService.record("operation", "banner:delete", "删除Banner", "BANNER", id, banner, null);
    }

    @Override
    public List<AdminConsoleVO.Notice> listNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<NoticeEntity>()
                        .orderByDesc(NoticeEntity::getPublishTime)
                        .orderByDesc(NoticeEntity::getId))
                .stream().map(this::toNotice).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.Notice saveNotice(Long id, AdminConsoleDTO.NoticeSave request) {
        validateTimeRange(request.getPublishTime(), request.getExpireTime());
        NoticeEntity notice = id == null ? new NoticeEntity() : requireNotice(id);
        NoticeEntity before = id == null ? null : copyNotice(notice);
        LocalDateTime now = LocalDateTime.now();
        notice.setNoticeType(request.getNoticeType().trim().toUpperCase(Locale.ROOT));
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setStatus(request.getStatus());
        notice.setPublishTime(request.getPublishTime());
        notice.setExpireTime(request.getExpireTime());
        notice.setUpdateAdminId(currentUserId());
        notice.setUpdateTime(now);
        if (id == null) {
            notice.setCreateAdminId(currentUserId());
            notice.setCreateTime(now);
            notice.setIsDeleted(0);
            noticeMapper.insert(notice);
        } else if (noticeMapper.updateById(notice) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "公告更新失败");
        }
        auditLogService.record("operation", id == null ? "notice:create" : "notice:update",
                id == null ? "新增公告" : "修改公告", "NOTICE", notice.getId(), before, notice);
        return toNotice(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        NoticeEntity notice = requireNotice(id);
        if (noticeMapper.deleteById(id) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "公告删除失败");
        }
        auditLogService.record("operation", "notice:delete", "删除公告", "NOTICE", id, notice, null);
    }

    @Override
    public List<AdminConsoleVO.Recommendation> listRecommendations() {
        return recommendationMapper.selectList(new LambdaQueryWrapper<HomeRecommendationEntity>()
                        .orderByAsc(HomeRecommendationEntity::getRecommendationType)
                        .orderByAsc(HomeRecommendationEntity::getSort))
                .stream().map(this::toRecommendation).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.Recommendation saveRecommendation(
            Long id, AdminConsoleDTO.RecommendationSave request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        String type = request.getRecommendationType().trim().toUpperCase(Locale.ROOT);
        if (!RECOMMENDATION_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "推荐类型不合法");
        }
        requireRecommendationTarget(type, request.getTargetId());
        HomeRecommendationEntity item = id == null
                ? new HomeRecommendationEntity() : requireRecommendation(id);
        HomeRecommendationEntity before = id == null ? null : copyRecommendation(item);
        LocalDateTime now = LocalDateTime.now();
        item.setRecommendationType(type);
        item.setTargetId(request.getTargetId());
        item.setTitleOverride(trimToNull(request.getTitleOverride()));
        item.setSort(request.getSort());
        item.setStatus(request.getStatus());
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        item.setUpdateAdminId(currentUserId());
        item.setUpdateTime(now);
        if (id == null) {
            item.setCreateTime(now);
            recommendationMapper.insert(item);
        } else if (recommendationMapper.updateById(item) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "首页推荐更新失败");
        }
        auditLogService.record("operation", id == null ? "recommendation:create" : "recommendation:update",
                id == null ? "新增首页推荐" : "修改首页推荐", "HOME_RECOMMENDATION",
                item.getId(), before, item);
        return toRecommendation(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecommendation(Long id) {
        HomeRecommendationEntity item = requireRecommendation(id);
        if (recommendationMapper.deleteById(id) != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "首页推荐删除失败");
        }
        auditLogService.record("operation", "recommendation:delete", "删除首页推荐",
                "HOME_RECOMMENDATION", id, item, null);
    }

    @Override
    public AdminConsoleVO.PublicHome getPublicHome() {
        LocalDateTime now = LocalDateTime.now();
        AdminConsoleVO.PublicHome result = new AdminConsoleVO.PublicHome();
        result.setBanners(bannerMapper.selectList(new LambdaQueryWrapper<BannerEntity>()
                        .eq(BannerEntity::getStatus, 1)
                        .and(q -> q.isNull(BannerEntity::getStartTime).or().le(BannerEntity::getStartTime, now))
                        .and(q -> q.isNull(BannerEntity::getEndTime).or().ge(BannerEntity::getEndTime, now))
                        .orderByAsc(BannerEntity::getSort))
                .stream().map(this::toBanner).toList());
        result.setNotices(noticeMapper.selectList(new LambdaQueryWrapper<NoticeEntity>()
                        .eq(NoticeEntity::getStatus, 1)
                        .and(q -> q.isNull(NoticeEntity::getPublishTime).or().le(NoticeEntity::getPublishTime, now))
                        .and(q -> q.isNull(NoticeEntity::getExpireTime).or().ge(NoticeEntity::getExpireTime, now))
                        .orderByDesc(NoticeEntity::getPublishTime))
                .stream().map(this::toNotice).toList());
        result.setRecommendations(recommendationMapper.selectList(
                        new LambdaQueryWrapper<HomeRecommendationEntity>()
                                .eq(HomeRecommendationEntity::getStatus, 1)
                                .and(q -> q.isNull(HomeRecommendationEntity::getStartTime)
                                        .or().le(HomeRecommendationEntity::getStartTime, now))
                                .and(q -> q.isNull(HomeRecommendationEntity::getEndTime)
                                        .or().ge(HomeRecommendationEntity::getEndTime, now))
                                .orderByAsc(HomeRecommendationEntity::getSort))
                .stream().map(this::toRecommendation).toList());
        result.setConfigs(configMapper.selectList(new LambdaQueryWrapper<SystemConfigEntity>()
                        .eq(SystemConfigEntity::getStatus, 1)
                        .eq(SystemConfigEntity::getSensitive, 0)
                        .in(SystemConfigEntity::getConfigGroup, "ERRAND", "USER", "HOME")
                        .orderByAsc(SystemConfigEntity::getId))
                .stream().map(this::toConfig).toList());
        return result;
    }

    private String normalizeConfigValue(SystemConfigEntity config, String value) {
        if (Integer.valueOf(1).equals(config.getSensitive()) && MASKED_VALUE.equals(value)) {
            return config.getConfigValue();
        }
        String normalized = value == null ? "" : value.trim();
        try {
            switch (config.getValueType()) {
                case "DECIMAL" -> new BigDecimal(normalized);
                case "INTEGER" -> Integer.parseInt(normalized);
                case "BOOLEAN" -> {
                    if (!"true".equalsIgnoreCase(normalized) && !"false".equalsIgnoreCase(normalized)) {
                        throw new IllegalArgumentException();
                    }
                }
                default -> {
                }
            }
        } catch (RuntimeException exception) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置值类型不正确");
        }
        return normalized;
    }

    private void requireRecommendationTarget(String type, Long targetId) {
        boolean exists = switch (type) {
            case "STORE" -> storeMapper.selectById(targetId) != null;
            case "PRODUCT", "HOT_PRODUCT" -> productMapper.selectById(targetId) != null;
            case "CATEGORY" -> categoryMapper.selectById(targetId) != null;
            default -> false;
        };
        if (!exists) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "推荐目标不存在");
        }
    }

    private String recommendationTargetName(HomeRecommendationEntity item) {
        return switch (item.getRecommendationType()) {
            case "STORE" -> storeMapper.selectById(item.getTargetId()) == null
                    ? "目标不存在" : storeMapper.selectById(item.getTargetId()).getStoreName();
            case "PRODUCT", "HOT_PRODUCT" -> productMapper.selectById(item.getTargetId()) == null
                    ? "目标不存在" : productMapper.selectById(item.getTargetId()).getProductName();
            case "CATEGORY" -> categoryMapper.selectById(item.getTargetId()) == null
                    ? "目标不存在" : categoryMapper.selectById(item.getTargetId()).getCategoryName();
            default -> "未知目标";
        };
    }

    private AdminConsoleVO.Config toConfig(SystemConfigEntity entity) {
        AdminConsoleVO.Config result = new AdminConsoleVO.Config();
        result.setId(entity.getId());
        result.setConfigGroup(entity.getConfigGroup());
        result.setConfigKey(entity.getConfigKey());
        result.setConfigValue(Integer.valueOf(1).equals(entity.getSensitive()) ? MASKED_VALUE : entity.getConfigValue());
        result.setValueType(entity.getValueType());
        result.setConfigName(entity.getConfigName());
        result.setConfigDescription(entity.getConfigDescription());
        result.setSensitive(entity.getSensitive());
        result.setStatus(entity.getStatus());
        result.setUpdateAdminId(entity.getUpdateAdminId());
        result.setUpdateTime(entity.getUpdateTime());
        return result;
    }

    private AdminConsoleVO.Banner toBanner(BannerEntity entity) {
        AdminConsoleVO.Banner result = new AdminConsoleVO.Banner();
        result.setId(entity.getId());
        result.setTitle(entity.getTitle());
        result.setSubtitle(entity.getSubtitle());
        result.setImageUrl(entity.getImageUrl());
        result.setTargetType(entity.getTargetType());
        result.setTargetValue(entity.getTargetValue());
        result.setSort(entity.getSort());
        result.setStatus(entity.getStatus());
        result.setStartTime(entity.getStartTime());
        result.setEndTime(entity.getEndTime());
        result.setUpdateTime(entity.getUpdateTime());
        return result;
    }

    private AdminConsoleVO.Notice toNotice(NoticeEntity entity) {
        AdminConsoleVO.Notice result = new AdminConsoleVO.Notice();
        result.setId(entity.getId());
        result.setNoticeType(entity.getNoticeType());
        result.setTitle(entity.getTitle());
        result.setContent(entity.getContent());
        result.setStatus(entity.getStatus());
        result.setPublishTime(entity.getPublishTime());
        result.setExpireTime(entity.getExpireTime());
        result.setUpdateTime(entity.getUpdateTime());
        return result;
    }

    private AdminConsoleVO.Recommendation toRecommendation(HomeRecommendationEntity entity) {
        AdminConsoleVO.Recommendation result = new AdminConsoleVO.Recommendation();
        result.setId(entity.getId());
        result.setRecommendationType(entity.getRecommendationType());
        result.setTargetId(entity.getTargetId());
        result.setTargetName(recommendationTargetName(entity));
        result.setTitleOverride(entity.getTitleOverride());
        result.setSort(entity.getSort());
        result.setStatus(entity.getStatus());
        result.setStartTime(entity.getStartTime());
        result.setEndTime(entity.getEndTime());
        result.setUpdateTime(entity.getUpdateTime());
        return result;
    }

    private SystemConfigEntity requireConfig(Long id) {
        SystemConfigEntity entity = id == null ? null : configMapper.selectById(id);
        if (entity == null) throw new BusinessException(ResultCode.PARAM_ERROR, "配置不存在");
        return entity;
    }

    private BannerEntity requireBanner(Long id) {
        BannerEntity entity = id == null ? null : bannerMapper.selectById(id);
        if (entity == null) throw new BusinessException(ResultCode.PARAM_ERROR, "Banner不存在");
        return entity;
    }

    private NoticeEntity requireNotice(Long id) {
        NoticeEntity entity = id == null ? null : noticeMapper.selectById(id);
        if (entity == null) throw new BusinessException(ResultCode.PARAM_ERROR, "公告不存在");
        return entity;
    }

    private HomeRecommendationEntity requireRecommendation(Long id) {
        HomeRecommendationEntity entity = id == null ? null : recommendationMapper.selectById(id);
        if (entity == null) throw new BusinessException(ResultCode.PARAM_ERROR, "首页推荐不存在");
        return entity;
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "结束时间不能早于开始时间");
        }
    }

    private Long currentUserId() {
        Long id = UserContext.getUserId();
        if (id == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        return id;
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private SystemConfigEntity copyConfig(SystemConfigEntity value) {
        SystemConfigEntity copy = new SystemConfigEntity();
        copy.setId(value.getId());
        copy.setConfigKey(value.getConfigKey());
        copy.setConfigValue(Integer.valueOf(1).equals(value.getSensitive()) ? MASKED_VALUE : value.getConfigValue());
        copy.setStatus(value.getStatus());
        return copy;
    }

    private BannerEntity copyBanner(BannerEntity value) {
        BannerEntity copy = new BannerEntity();
        copy.setId(value.getId()); copy.setTitle(value.getTitle()); copy.setImageUrl(value.getImageUrl());
        copy.setStatus(value.getStatus()); copy.setSort(value.getSort());
        return copy;
    }

    private NoticeEntity copyNotice(NoticeEntity value) {
        NoticeEntity copy = new NoticeEntity();
        copy.setId(value.getId()); copy.setTitle(value.getTitle()); copy.setStatus(value.getStatus());
        return copy;
    }

    private HomeRecommendationEntity copyRecommendation(HomeRecommendationEntity value) {
        HomeRecommendationEntity copy = new HomeRecommendationEntity();
        copy.setId(value.getId()); copy.setRecommendationType(value.getRecommendationType());
        copy.setTargetId(value.getTargetId()); copy.setStatus(value.getStatus()); copy.setSort(value.getSort());
        return copy;
    }
}
