package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.service.AdminOverviewService;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/finance")
@AdminPermission("finance:view")
public class AdminFinanceController {
    private final AdminOverviewService service;

    public AdminFinanceController(AdminOverviewService service) { this.service = service; }

    @GetMapping("/summary")
    public Result<AdminConsoleVO.Finance> summary() { return Result.success(service.finance()); }
}
