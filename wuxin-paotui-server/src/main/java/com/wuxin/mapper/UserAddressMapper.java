package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuxin.entity.UserAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {

    @Select("SELECT * FROM user_address WHERE id = #{id}")
    UserAddressEntity selectByIdIncludeDeleted(@Param("id") Long id);
}
