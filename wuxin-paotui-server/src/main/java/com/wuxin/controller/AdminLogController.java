package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminSecurityService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@AdminPermission("log:view")
public class AdminLogController {
    private final AdminSecurityService service;

    public AdminLogController(AdminSecurityService service) { this.service = service; }

    @GetMapping
    public Result<PageResultVO<AdminConsoleVO.OperationLog>> page(@Valid AdminConsoleDTO.LogQuery query) {
        return Result.success(service.logs(query));
    }
}
