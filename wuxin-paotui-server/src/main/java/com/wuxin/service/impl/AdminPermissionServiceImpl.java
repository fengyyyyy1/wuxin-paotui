package com.wuxin.service.impl;

import com.wuxin.enums.AdminRoleEnum;
import com.wuxin.mapper.AdminPermissionMapper;
import com.wuxin.service.AdminPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private final AdminPermissionMapper adminPermissionMapper;

    public AdminPermissionServiceImpl(
            AdminPermissionMapper adminPermissionMapper) {
        this.adminPermissionMapper = adminPermissionMapper;
    }

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        List<String> roles = getRoleCodes(userId);
        return roles.contains(AdminRoleEnum.ADMIN.getCode())
                || roles.contains("SUPER_ADMIN")
                || roles.contains("OPERATIONS")
                || roles.contains("CUSTOMER_SERVICE")
                || roles.contains("AUDITOR")
                || roles.contains("FINANCE");
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }
        List<String> roles = getRoleCodes(userId);
        if (roles.contains(AdminRoleEnum.ADMIN.getCode()) || roles.contains("SUPER_ADMIN")) {
            return true;
        }
        return getPermissionCodes(userId).contains(permissionCode);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        return userId == null ? List.of() : adminPermissionMapper.selectActiveRoleCodes(userId);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        return userId == null ? List.of() : adminPermissionMapper.selectActivePermissionCodes(userId);
    }
}
