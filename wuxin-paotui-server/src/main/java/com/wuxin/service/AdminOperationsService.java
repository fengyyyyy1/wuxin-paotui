package com.wuxin.service;

import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.vo.admin.AdminConsoleVO;

import java.math.BigDecimal;
import java.util.List;

public interface AdminOperationsService {
    List<AdminConsoleVO.Config> listConfigs(String group);
    AdminConsoleVO.Config updateConfig(Long id, AdminConsoleDTO.ConfigUpdate request);
    BigDecimal getDecimal(String key, BigDecimal defaultValue);
    List<AdminConsoleVO.Banner> listBanners();
    AdminConsoleVO.Banner saveBanner(Long id, AdminConsoleDTO.BannerSave request);
    void deleteBanner(Long id);
    List<AdminConsoleVO.Notice> listNotices();
    AdminConsoleVO.Notice saveNotice(Long id, AdminConsoleDTO.NoticeSave request);
    void deleteNotice(Long id);
    List<AdminConsoleVO.Recommendation> listRecommendations();
    AdminConsoleVO.Recommendation saveRecommendation(
            Long id, AdminConsoleDTO.RecommendationSave request);
    void deleteRecommendation(Long id);
    AdminConsoleVO.PublicHome getPublicHome();
}
