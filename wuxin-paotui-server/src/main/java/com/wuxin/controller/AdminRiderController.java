package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.dto.admin.AdminRiderReasonDTO;
import com.wuxin.service.AdminBusinessService;
import com.wuxin.service.AdminRiderService;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.vo.AdminRiderOperationVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rider")
@AdminPermission("rider:view")
public class AdminRiderController {

    private final AdminRiderService adminRiderService;
    private final AdminBusinessService adminBusinessService;
    private final AdminAuditLogService auditLogService;

    public AdminRiderController(
            AdminRiderService adminRiderService,
            AdminBusinessService adminBusinessService,
            AdminAuditLogService auditLogService) {
        this.adminRiderService = adminRiderService;
        this.adminBusinessService = adminBusinessService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/page")
    public Result<PageResultVO<AdminConsoleVO.RiderRow>> page(
            @Valid AdminConsoleDTO.RiderQuery query) {
        return Result.success(adminBusinessService.pageRiders(query));
    }

    @GetMapping("/{id}")
    public Result<AdminConsoleVO.RiderRow> detail(@PathVariable Long id) {
        return Result.success(adminBusinessService.riderDetail(id));
    }

    @PostMapping("/{id}/approve")
    @AdminPermission("rider:audit")
    public Result<AdminRiderOperationVO> approve(@PathVariable Long id) {
        AdminRiderOperationVO result = adminRiderService.approve(id);
        record("rider:approve", "骑手审核通过", id, result);
        return Result.success("骑手审核通过", result);
    }

    @PostMapping("/{id}/reject")
    @AdminPermission("rider:audit")
    public Result<AdminRiderOperationVO> reject(
            @PathVariable Long id,
            @Valid @RequestBody AdminRiderReasonDTO request) {
        AdminRiderOperationVO result = adminRiderService.reject(id, request);
        record("rider:reject", "骑手审核拒绝", id, result);
        return Result.success("骑手审核已拒绝", result);
    }

    @PostMapping("/{id}/enable")
    @AdminPermission("rider:manage")
    public Result<AdminRiderOperationVO> enable(@PathVariable Long id) {
        AdminRiderOperationVO result = adminRiderService.enable(id);
        record("rider:enable", "启用骑手", id, result);
        return Result.success("骑手账号已启用", result);
    }

    @PostMapping("/{id}/disable")
    @AdminPermission("rider:manage")
    public Result<AdminRiderOperationVO> disable(
            @PathVariable Long id,
            @Valid @RequestBody AdminRiderReasonDTO request) {
        AdminRiderOperationVO result = adminRiderService.disable(id, request);
        record("rider:disable", "禁用骑手", id, result);
        return Result.success("骑手账号已禁用", result);
    }

    private void record(String code, String name, Long id, AdminRiderOperationVO result) {
        auditLogService.record("rider", code, name, "RIDER", id, null, result);
    }
}
