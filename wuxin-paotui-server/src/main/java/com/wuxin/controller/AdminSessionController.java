package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.AdminSecurityService;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/session")
public class AdminSessionController {
    private final AdminSecurityService service;

    public AdminSessionController(AdminSecurityService service) {
        this.service = service;
    }

    @GetMapping
    public Result<AdminConsoleVO.Session> session() {
        return Result.success(service.session());
    }
}
