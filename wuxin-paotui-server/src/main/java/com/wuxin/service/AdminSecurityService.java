package com.wuxin.service;

import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;

import java.util.List;

public interface AdminSecurityService {
    AdminConsoleVO.Session session();
    List<AdminConsoleVO.Role> roles();
    List<AdminConsoleVO.Permission> permissions();
    List<AdminConsoleVO.AdminUser> adminUsers();
    void updateUserRoles(Long userId, AdminConsoleDTO.UserRolesUpdate request);
    void updateRolePermissions(Long roleId, AdminConsoleDTO.RolePermissionsUpdate request);
    PageResultVO<AdminConsoleVO.OperationLog> logs(AdminConsoleDTO.LogQuery query);
}
