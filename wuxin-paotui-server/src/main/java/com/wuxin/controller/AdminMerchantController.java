package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminMerchantPageQueryDTO;
import com.wuxin.dto.admin.AdminMerchantReasonDTO;
import com.wuxin.dto.admin.ApproveMerchantDTO;
import com.wuxin.service.AdminMerchantService;
import com.wuxin.service.AdminAuditLogService;
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
@AdminPermission("merchant:view")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;
    private final AdminAuditLogService auditLogService;

    public AdminMerchantController(
            AdminMerchantService adminMerchantService,
            AdminAuditLogService auditLogService) {
        this.adminMerchantService = adminMerchantService;
        this.auditLogService = auditLogService;
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
    @AdminPermission("merchant:audit")
    public Result<AdminMerchantOperationVO> approve(
            @PathVariable Long merchantId,
            @Valid @RequestBody ApproveMerchantDTO request) {
        AdminMerchantOperationVO result = adminMerchantService.approve(merchantId, request);
        record("merchant:approve", "商家审核通过", merchantId, result);
        return Result.success("商家审核通过", result);
    }

    @PostMapping("/{merchantId}/reject")
    @AdminPermission("merchant:audit")
    public Result<AdminMerchantOperationVO> reject(
            @PathVariable Long merchantId,
            @Valid @RequestBody AdminMerchantReasonDTO request) {
        AdminMerchantOperationVO result = adminMerchantService.reject(merchantId, request);
        record("merchant:reject", "商家审核拒绝", merchantId, result);
        return Result.success("商家审核拒绝", result);
    }

    @PostMapping("/{merchantId}/enable")
    @AdminPermission("merchant:manage")
    public Result<AdminMerchantOperationVO> enable(
            @PathVariable Long merchantId) {
        AdminMerchantOperationVO result = adminMerchantService.enable(merchantId);
        record("merchant:enable", "启用商家", merchantId, result);
        return Result.success("商家启用成功", result);
    }

    @PostMapping("/{merchantId}/disable")
    @AdminPermission("merchant:manage")
    public Result<AdminMerchantOperationVO> disable(
            @PathVariable Long merchantId,
            @Valid @RequestBody AdminMerchantReasonDTO request) {
        AdminMerchantOperationVO result = adminMerchantService.disable(merchantId, request);
        record("merchant:disable", "禁用商家", merchantId, result);
        return Result.success("商家禁用成功", result);
    }

    private void record(String code, String name, Long id, AdminMerchantOperationVO result) {
        auditLogService.record("merchant", code, name, "MERCHANT", id, null, result);
    }
}
