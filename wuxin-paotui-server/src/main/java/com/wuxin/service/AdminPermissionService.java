package com.wuxin.service;

import java.util.List;

public interface AdminPermissionService {

    boolean isAdmin(Long userId);

    boolean hasPermission(Long userId, String permissionCode);

    List<String> getRoleCodes(Long userId);

    List<String> getPermissionCodes(Long userId);
}
