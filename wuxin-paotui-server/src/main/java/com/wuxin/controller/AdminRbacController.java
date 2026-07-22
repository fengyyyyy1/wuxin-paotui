package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminSecurityService;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rbac")
@AdminPermission("rbac:view")
public class AdminRbacController {
    private final AdminSecurityService service;

    public AdminRbacController(AdminSecurityService service) { this.service = service; }

    @GetMapping("/roles")
    public Result<List<AdminConsoleVO.Role>> roles() { return Result.success(service.roles()); }

    @GetMapping("/permissions")
    public Result<List<AdminConsoleVO.Permission>> permissions() { return Result.success(service.permissions()); }

    @GetMapping("/users")
    public Result<List<AdminConsoleVO.AdminUser>> users() { return Result.success(service.adminUsers()); }

    @PutMapping("/users/{id}/roles")
    @AdminPermission("rbac:manage")
    public Result<Void> updateUserRoles(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.UserRolesUpdate request) {
        service.updateUserRoles(id, request); return Result.success();
    }

    @PutMapping("/roles/{id}/permissions")
    @AdminPermission("rbac:manage")
    public Result<Void> updateRolePermissions(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.RolePermissionsUpdate request) {
        service.updateRolePermissions(id, request); return Result.success();
    }
}
