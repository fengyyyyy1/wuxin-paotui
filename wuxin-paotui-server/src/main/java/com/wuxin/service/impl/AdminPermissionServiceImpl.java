package com.wuxin.service.impl;

import com.wuxin.enums.AdminRoleEnum;
import com.wuxin.mapper.AdminPermissionMapper;
import com.wuxin.service.AdminPermissionService;
import org.springframework.stereotype.Service;

@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private final AdminPermissionMapper adminPermissionMapper;

    public AdminPermissionServiceImpl(
            AdminPermissionMapper adminPermissionMapper) {
        this.adminPermissionMapper = adminPermissionMapper;
    }

    @Override
    public boolean isAdmin(Long userId) {
        return userId != null
                && adminPermissionMapper.countActiveUserRole(
                        userId,
                        AdminRoleEnum.ADMIN.getCode()) > 0;
    }
}
