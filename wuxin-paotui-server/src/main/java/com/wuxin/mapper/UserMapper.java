package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuxin.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    long countByUsernameIncludingDeleted(@Param("username") String username);

    @Select("""
            SELECT COUNT(*)
            FROM sys_user
            WHERE phone = #{phone}
              AND id <> #{userId}
              AND is_deleted = 0
            """)
    long countActiveUsersByPhoneExcludingUser(
            @Param("phone") String phone,
            @Param("userId") Long userId);
}
