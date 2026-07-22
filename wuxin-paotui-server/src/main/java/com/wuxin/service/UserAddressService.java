package com.wuxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuxin.dto.UserAddressDTO;
import com.wuxin.entity.UserAddressEntity;

import java.util.List;

public interface UserAddressService extends IService<UserAddressEntity> {

    void addAddress(Long userId, UserAddressDTO userAddressDTO);

    List<UserAddressEntity> listAddress(Long userId);

    void updateAddress(Long userId, Long addressId, UserAddressDTO userAddressDTO);

    void setDefaultAddress(Long userId, Long addressId);

    void deleteAddress(Long userId, Long addressId);
}
