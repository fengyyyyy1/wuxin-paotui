package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminMerchantPageQueryDTO;
import com.wuxin.dto.admin.AdminMerchantReasonDTO;
import com.wuxin.dto.admin.ApproveMerchantDTO;
import com.wuxin.service.AdminMerchantService;
import com.wuxin.vo.AdminMerchantDetailVO;
import com.wuxin.vo.AdminMerchantOperationVO;
import com.wuxin.vo.AdminMerchantPageVO;
import com.wuxin.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    public AdminMerchantController(
            AdminMerchantService adminMerchantService) {
        this.adminMerchantService = adminMerchantService;
    }

    @GetMapping("/page")
    public Result<PageResultVO<AdminMerchantPageVO>> page(
            @Valid @ModelAttribute AdminMerchantPageQueryDTO query) {
        return Result.success(adminMerchantService.page(query));
    }

    @GetMapping("/{merchantId}")
    public Result<AdminMerchantDetailVO> detail(
            @PathVariable Long merchantId) {
        return Result.success(adminMerchantService.detail(merchantId));
    }

    @PostMapping("/{merchantId}/approve")
    public Result<AdminMerchantOperationVO> approve(
            @PathVariable Long merchantId,
            @Valid @RequestBody ApproveMerchantDTO request) {
        return Result.success(
                "商家审核通过",
                adminMerchantService.approve(merchantId, request));
    }

    @PostMapping("/{merchantId}/reject")
    public Result<AdminMerchantOperationVO> reject(
            @PathVariable Long merchantId,
            @Valid @RequestBody AdminMerchantReasonDTO request) {
        return Result.success(
                "商家审核拒绝",
                adminMerchantService.reject(merchantId, request));
    }

    @PostMapping("/{merchantId}/enable")
    public Result<AdminMerchantOperationVO> enable(
            @PathVariable Long merchantId) {
        return Result.success(
                "商家启用成功",
                adminMerchantService.enable(merchantId));
    }

    @PostMapping("/{merchantId}/disable")
    public Result<AdminMerchantOperationVO> disable(
            @PathVariable Long merchantId,
            @Valid @RequestBody AdminMerchantReasonDTO request) {
        return Result.success(
                "商家禁用成功",
                adminMerchantService.disable(merchantId, request));
    }
}
