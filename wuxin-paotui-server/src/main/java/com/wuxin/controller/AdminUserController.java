package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminBusinessService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@AdminPermission("user:view")
public class AdminUserController {
    private final AdminBusinessService service;

    public AdminUserController(AdminBusinessService service) {
        this.service = service;
    }

    @GetMapping
    public Result<PageResultVO<AdminConsoleVO.UserRow>> page(@Valid AdminConsoleDTO.UserQuery query) {
        return Result.success(service.pageUsers(query));
    }

    @PutMapping("/{id}/status")
    @AdminPermission("user:manage")
    public Result<AdminConsoleVO.UserRow> updateStatus(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.StatusUpdate request) {
        return Result.success("用户状态已更新", service.updateUserStatus(id, request));
    }
}
