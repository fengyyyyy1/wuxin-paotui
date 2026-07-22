package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.UserAddressDTO;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.service.UserAddressService;
import com.wuxin.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        userAddressService.addAddress(getRequiredUserId(), userAddressDTO);
        return Result.success("新增地址成功");
    }

    @GetMapping("/list")
    public Result<List<UserAddressEntity>> list() {
        return Result.success(userAddressService.listAddress(getRequiredUserId()));
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody UserAddressDTO userAddressDTO) {
        userAddressService.updateAddress(getRequiredUserId(), id, userAddressDTO);
        return Result.success("修改地址成功");
    }

    @PutMapping("/{id}/default")
    public Result<String> setDefault(@PathVariable Long id) {
        userAddressService.setDefaultAddress(getRequiredUserId(), id);
        return Result.success("设置默认地址成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        userAddressService.deleteAddress(getRequiredUserId(), id);
        return Result.success("删除地址成功");
    }

    private Long getRequiredUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
