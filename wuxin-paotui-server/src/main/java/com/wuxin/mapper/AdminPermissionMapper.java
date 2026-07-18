package com.wuxin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminPermissionMapper {

    @Select("""
            SELECT COUNT(*)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id
            INNER JOIN sys_user u ON u.id = ur.user_id
            WHERE ur.user_id = #{userId}
              AND r.role_code = #{roleCode}
              AND u.status = 1
              AND u.is_deleted = 0
            """)
    long countActiveUserRole(
            @Param("userId") Long userId,
            @Param("roleCode") String roleCode);
}
