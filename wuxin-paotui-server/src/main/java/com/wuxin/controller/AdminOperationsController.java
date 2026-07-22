package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminOperationsService;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operations")
@AdminPermission("operation:view")
public class AdminOperationsController {
    private final AdminOperationsService service;

    public AdminOperationsController(AdminOperationsService service) {
        this.service = service;
    }

    @GetMapping("/banners")
    public Result<List<AdminConsoleVO.Banner>> banners() { return Result.success(service.listBanners()); }

    @PostMapping("/banners")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Banner> createBanner(@Valid @RequestBody AdminConsoleDTO.BannerSave request) {
        return Result.success("Banner已创建", service.saveBanner(null, request));
    }

    @PutMapping("/banners/{id}")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Banner> updateBanner(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.BannerSave request) {
        return Result.success("Banner已更新", service.saveBanner(id, request));
    }

    @DeleteMapping("/banners/{id}")
    @AdminPermission("operation:manage")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        service.deleteBanner(id); return Result.success();
    }

    @GetMapping("/notices")
    public Result<List<AdminConsoleVO.Notice>> notices() { return Result.success(service.listNotices()); }

    @PostMapping("/notices")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Notice> createNotice(@Valid @RequestBody AdminConsoleDTO.NoticeSave request) {
        return Result.success("公告已创建", service.saveNotice(null, request));
    }

    @PutMapping("/notices/{id}")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Notice> updateNotice(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.NoticeSave request) {
        return Result.success("公告已更新", service.saveNotice(id, request));
    }

    @DeleteMapping("/notices/{id}")
    @AdminPermission("operation:manage")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        service.deleteNotice(id); return Result.success();
    }

    @GetMapping("/recommendations")
    public Result<List<AdminConsoleVO.Recommendation>> recommendations() {
        return Result.success(service.listRecommendations());
    }

    @PostMapping("/recommendations")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Recommendation> createRecommendation(
            @Valid @RequestBody AdminConsoleDTO.RecommendationSave request) {
        return Result.success("首页推荐已创建", service.saveRecommendation(null, request));
    }

    @PutMapping("/recommendations/{id}")
    @AdminPermission("operation:manage")
    public Result<AdminConsoleVO.Recommendation> updateRecommendation(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.RecommendationSave request) {
        return Result.success("首页推荐已更新", service.saveRecommendation(id, request));
    }

    @DeleteMapping("/recommendations/{id}")
    @AdminPermission("operation:manage")
    public Result<Void> deleteRecommendation(@PathVariable Long id) {
        service.deleteRecommendation(id); return Result.success();
    }
}
