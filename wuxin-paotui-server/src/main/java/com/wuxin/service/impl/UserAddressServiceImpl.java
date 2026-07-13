package com.wuxin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.service.UserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddressEntity>
        implements UserAddressService {
}
