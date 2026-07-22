package com.wuxin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.UserAddressDTO;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddressEntity>
        implements UserAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, UserAddressDTO userAddressDTO) {
        if (Integer.valueOf(1).equals(userAddressDTO.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        UserAddressEntity userAddressEntity = new UserAddressEntity();
        userAddressEntity.setUserId(userId);
        copyAddress(userAddressEntity, userAddressDTO);
        userAddressEntity.setIsDefault(Integer.valueOf(1).equals(userAddressDTO.getIsDefault()) ? 1 : 0);
        save(userAddressEntity);
    }

    @Override
    public List<UserAddressEntity> listAddress(Long userId) {
        return lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .orderByDesc(UserAddressEntity::getIsDefault)
                .orderByDesc(UserAddressEntity::getCreateTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, UserAddressDTO userAddressDTO) {
        UserAddressEntity address = getActiveAddress(userId, addressId);
        if (Integer.valueOf(1).equals(userAddressDTO.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        copyAddress(address, userAddressDTO);
        address.setIsDefault(Integer.valueOf(1).equals(userAddressDTO.getIsDefault()) ? 1 : 0);
        boolean updated = updateById(address);
        if (!updated) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST, "地址不存在或无权操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        getActiveAddress(userId, addressId);
        clearDefaultAddress(userId);
        boolean updated = lambdaUpdate()
                .eq(UserAddressEntity::getId, addressId)
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .set(UserAddressEntity::getIsDefault, 1)
                .update();
        if (!updated) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST, "地址不存在或无权操作");
        }
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        boolean updated = lambdaUpdate()
                .eq(UserAddressEntity::getId, addressId)
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .set(UserAddressEntity::getIsDeleted, 1)
                .update();
        if (!updated) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST, "地址不存在或无权操作");
        }
    }

    private UserAddressEntity getActiveAddress(Long userId, Long addressId) {
        UserAddressEntity address = lambdaQuery()
                .eq(UserAddressEntity::getId, addressId)
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .one();
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST, "地址不存在或无权操作");
        }
        return address;
    }

    private void clearDefaultAddress(Long userId) {
        lambdaUpdate()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .eq(UserAddressEntity::getIsDefault, 1)
                .set(UserAddressEntity::getIsDefault, 0)
                .update();
    }

    private void copyAddress(UserAddressEntity address, UserAddressDTO userAddressDTO) {
        address.setReceiverName(userAddressDTO.getReceiverName());
        address.setReceiverPhone(userAddressDTO.getReceiverPhone());
        address.setProvince(userAddressDTO.getProvince());
        address.setCity(userAddressDTO.getCity());
        address.setDistrict(userAddressDTO.getDistrict());
        address.setDetailAddress(userAddressDTO.getDetailAddress());
        address.setLatitude(userAddressDTO.getLatitude());
        address.setLongitude(userAddressDTO.getLongitude());
    }
}
