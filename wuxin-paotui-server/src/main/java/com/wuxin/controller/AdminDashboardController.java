package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.service.AdminOverviewService;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@AdminPermission("dashboard:view")
public class AdminDashboardController {
    private final AdminOverviewService service;

    public AdminDashboardController(AdminOverviewService service) {
        this.service = service;
    }

    @GetMapping
    public Result<AdminConsoleVO.Dashboard> dashboard() {
        return Result.success(service.dashboard());
    }
}
