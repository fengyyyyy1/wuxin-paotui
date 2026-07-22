package com.wuxin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminPermissionMapper {

    @Select("""
            SELECT COUNT(*)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id
            INNER JOIN sys_user u ON u.id = ur.user_id
            WHERE ur.user_id = #{userId}
              AND r.role_code = #{roleCode}
              AND COALESCE(r.status, 1) = 1
              AND u.status = 1
              AND u.is_deleted = 0
            """)
    long countActiveUserRole(
            @Param("userId") Long userId,
            @Param("roleCode") String roleCode);

    @Select("""
            SELECT DISTINCT r.role_code
            FROM sys_user_role ur
            JOIN sys_role r ON r.id = ur.role_id
            JOIN sys_user u ON u.id = ur.user_id
            WHERE ur.user_id = #{userId}
              AND COALESCE(r.status, 1) = 1
              AND u.status = 1
              AND u.is_deleted = 0
            ORDER BY r.role_code
            """)
    List<String> selectActiveRoleCodes(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.permission_code
            FROM sys_user_role ur
            JOIN sys_role r ON r.id = ur.role_id AND COALESCE(r.status, 1) = 1
            JOIN sys_role_permission rp ON rp.role_id = r.id
            JOIN sys_permission p ON p.id = rp.permission_id AND p.status = 1
            JOIN sys_user u ON u.id = ur.user_id
            WHERE ur.user_id = #{userId}
              AND u.status = 1
              AND u.is_deleted = 0
            ORDER BY p.permission_code
            """)
    List<String> selectActivePermissionCodes(@Param("userId") Long userId);
}
