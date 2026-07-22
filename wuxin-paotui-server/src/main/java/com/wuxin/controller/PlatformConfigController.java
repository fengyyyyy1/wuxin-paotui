package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.AdminOperationsService;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformConfigController {
    private final AdminOperationsService service;

    public PlatformConfigController(AdminOperationsService service) { this.service = service; }

    @GetMapping("/home")
    public Result<AdminConsoleVO.PublicHome> home() { return Result.success(service.getPublicHome()); }
}
