package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminRiderReasonDTO;
import com.wuxin.service.AdminRiderService;
import com.wuxin.vo.AdminRiderOperationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rider")
public class AdminRiderController {

    private final AdminRiderService adminRiderService;

    public AdminRiderController(AdminRiderService adminRiderService) {
        this.adminRiderService = adminRiderService;
    }

    @PostMapping("/{id}/approve")
    public Result<AdminRiderOperationVO> approve(@PathVariable Long id) {
        return Result.success("骑手审核通过", adminRiderService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<AdminRiderOperationVO> reject(
            @PathVariable Long id,
            @Valid @RequestBody AdminRiderReasonDTO request) {
        return Result.success("骑手审核已拒绝", adminRiderService.reject(id, request));
    }

    @PostMapping("/{id}/enable")
    public Result<AdminRiderOperationVO> enable(@PathVariable Long id) {
        return Result.success("骑手账号已启用", adminRiderService.enable(id));
    }

    @PostMapping("/{id}/disable")
    public Result<AdminRiderOperationVO> disable(
            @PathVariable Long id,
            @Valid @RequestBody AdminRiderReasonDTO request) {
        return Result.success("骑手账号已禁用", adminRiderService.disable(id, request));
    }
}
