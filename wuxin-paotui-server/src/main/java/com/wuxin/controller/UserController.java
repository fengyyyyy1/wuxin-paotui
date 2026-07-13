package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.UserLoginDTO;
import com.wuxin.dto.UserRegisterDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.service.UserService;
import com.wuxin.utils.JwtUtils;
import com.wuxin.utils.PasswordUtils;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.LoginVO;
import com.wuxin.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class    UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public Result<List<UserEntity>> list() {
        return Result.success(userService.list());
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        boolean exists = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userRegisterDTO.getUsername())
                .exists();
        if (exists) {
            return Result.fail(ResultCode.USERNAME_EXIST);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRegisterDTO.getUsername());
        userEntity.setPassword(PasswordUtils.encode(userRegisterDTO.getPassword()));
        userEntity.setPhone(userRegisterDTO.getPhone());
        userService.save(userEntity);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, userLoginDTO.getUsername())
                .one();
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }
        if (!PasswordUtils.matches(userLoginDTO.getPassword(), user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR);
        }

        UserInfoVO userInfoVO = buildUserInfoVO(user);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(JwtUtils.generateToken(user.getId(), user.getUsername()));
        loginVO.setUserInfo(userInfoVO);
        return Result.success(loginVO);
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }

        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(buildUserInfoVO(user));
    }

    private UserInfoVO buildUserInfoVO(UserEntity user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setPhone(user.getPhone());
        return userInfoVO;
    }
}
