package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.UserAddressDTO;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.service.UserAddressService;
import com.wuxin.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @PostMapping
    public Result<String> add(@Valid @RequestBody UserAddressDTO userAddressDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        if (Integer.valueOf(1).equals(userAddressDTO.getIsDefault())) {
            userAddressService.lambdaUpdate()
                    .eq(UserAddressEntity::getUserId, userId)
                    .eq(UserAddressEntity::getIsDeleted, 0)
                    .set(UserAddressEntity::getIsDefault, 0)
                    .update();
        }

        UserAddressEntity userAddressEntity = new UserAddressEntity();
        userAddressEntity.setUserId(userId);
        userAddressEntity.setReceiverName(userAddressDTO.getReceiverName());
        userAddressEntity.setReceiverPhone(userAddressDTO.getReceiverPhone());
        userAddressEntity.setProvince(userAddressDTO.getProvince());
        userAddressEntity.setCity(userAddressDTO.getCity());
        userAddressEntity.setDistrict(userAddressDTO.getDistrict());
        userAddressEntity.setDetailAddress(userAddressDTO.getDetailAddress());
        userAddressEntity.setLatitude(userAddressDTO.getLatitude());
        userAddressEntity.setLongitude(userAddressDTO.getLongitude());
        userAddressEntity.setIsDefault(Integer.valueOf(1).equals(userAddressDTO.getIsDefault()) ? 1 : 0);
        userAddressService.save(userAddressEntity);
        return Result.success("新增地址成功");
    }

    @GetMapping("/list")
    public Result<List<UserAddressEntity>> list() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        List<UserAddressEntity> addressList = userAddressService.lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .orderByDesc(UserAddressEntity::getIsDefault)
                .orderByDesc(UserAddressEntity::getCreateTime)
                .list();
        return Result.success(addressList);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        boolean updated = userAddressService.lambdaUpdate()
                .eq(UserAddressEntity::getId, id)
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDeleted, 0)
                .set(UserAddressEntity::getIsDeleted, 1)
                .update();
        if (!updated) {
            return Result.fail(ResultCode.PARAM_ERROR, "地址不存在或无权操作");
        }

        return Result.success("删除地址成功");
    }
}
