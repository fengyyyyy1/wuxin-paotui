package com.wuxin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.AdminConsoleMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.service.AdminPermissionService;
import com.wuxin.service.AdminSecurityService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminSecurityServiceImpl implements AdminSecurityService {

    private final AdminConsoleMapper mapper;
    private final UserMapper userMapper;
    private final AdminPermissionService permissionService;
    private final AdminAuditLogService auditLogService;

    public AdminSecurityServiceImpl(
            AdminConsoleMapper mapper,
            UserMapper userMapper,
            AdminPermissionService permissionService,
            AdminAuditLogService auditLogService) {
        this.mapper = mapper;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
        this.auditLogService = auditLogService;
    }

    @Override
    public AdminConsoleVO.Session session() {
        Long userId = currentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);
        AdminConsoleVO.Session result = new AdminConsoleVO.Session();
        result.setUserId(userId);
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setRoles(permissionService.getRoleCodes(userId));
        result.setPermissions(permissionService.getPermissionCodes(userId));
        return result;
    }

    @Override
    public List<AdminConsoleVO.Role> roles() {
        List<AdminConsoleVO.Role> roles = mapper.selectRoles();
        roles.forEach(role -> role.setPermissionIds(mapper.selectRolePermissionIds(role.getRoleId())));
        return roles;
    }

    @Override
    public List<AdminConsoleVO.Permission> permissions() {
        return mapper.selectPermissions();
    }

    @Override
    public List<AdminConsoleVO.AdminUser> adminUsers() {
        List<AdminConsoleVO.AdminUser> users = mapper.selectAdminUsers();
        users.forEach(user -> {
            user.setRoleIds(mapper.selectUserRoleIds(user.getUserId()));
            user.setRoleNames(mapper.selectUserRoleNames(user.getUserId()));
        });
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, AdminConsoleDTO.UserRolesUpdate request) {
        if (userMapper.selectById(userId) == null) throw new BusinessException(ResultCode.USER_NOT_EXIST);
        if (userId.equals(currentUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能修改当前登录管理员自己的角色");
        }
        List<Long> nextRoleIds = request.getRoleIds().stream().distinct().toList();
        Set<Long> activeRoleIds = mapper.selectRoles().stream()
                .filter(role -> Integer.valueOf(1).equals(role.getStatus()))
                .map(AdminConsoleVO.Role::getRoleId)
                .collect(Collectors.toSet());
        if (!activeRoleIds.containsAll(nextRoleIds)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在或已停用");
        }
        List<Long> before = mapper.selectUserRoleIds(userId);
        mapper.deleteUserRoles(userId);
        mapper.insertUserRoles(userId, nextRoleIds);
        auditLogService.record("rbac", "rbac:user-roles", "修改管理员角色",
                "USER", userId, before, nextRoleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, AdminConsoleDTO.RolePermissionsUpdate request) {
        if (mapper.selectRoles().stream().noneMatch(role -> roleId.equals(role.getRoleId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }
        List<Long> nextPermissionIds = request.getPermissionIds().stream().distinct().toList();
        Set<Long> activePermissionIds = mapper.selectPermissions().stream()
                .filter(permission -> Integer.valueOf(1).equals(permission.getStatus()))
                .map(AdminConsoleVO.Permission::getPermissionId)
                .collect(Collectors.toSet());
        if (!activePermissionIds.containsAll(nextPermissionIds)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限不存在或已停用");
        }
        List<Long> before = mapper.selectRolePermissionIds(roleId);
        mapper.deleteRolePermissions(roleId);
        mapper.insertRolePermissions(roleId, nextPermissionIds);
        auditLogService.record("rbac", "rbac:role-permissions", "修改角色权限",
                "ROLE", roleId, before, nextPermissionIds);
    }

    @Override
    public PageResultVO<AdminConsoleVO.OperationLog> logs(AdminConsoleDTO.LogQuery query) {
        Page<AdminConsoleVO.OperationLog> page = mapper.selectOperationLogPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getAdminUserId(),
                query.getModuleCode(), query.getResultStatus(), query.getStartTime(),
                query.getEndTime(), query.getKeyword());
        PageResultVO<AdminConsoleVO.OperationLog> result = new PageResultVO<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    private Long currentUserId() {
        Long id = UserContext.getUserId();
        if (id == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        return id;
    }
}
