package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminOperationsService;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/configs")
@AdminPermission("config:view")
public class AdminConfigController {
    private final AdminOperationsService service;

    public AdminConfigController(AdminOperationsService service) { this.service = service; }

    @GetMapping
    public Result<List<AdminConsoleVO.Config>> list(@RequestParam(required = false) String group) {
        return Result.success(service.listConfigs(group));
    }

    @PutMapping("/{id}")
    @AdminPermission("config:manage")
    public Result<AdminConsoleVO.Config> update(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.ConfigUpdate request) {
        return Result.success("配置已生效", service.updateConfig(id, request));
    }
}
