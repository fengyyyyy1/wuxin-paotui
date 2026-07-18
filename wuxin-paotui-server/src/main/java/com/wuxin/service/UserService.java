package com.wuxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuxin.dto.user.UpdateUserProfileDTO;
import com.wuxin.dto.wechat.BindWechatPhoneDTO;
import com.wuxin.dto.wechat.WeChatLoginDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.vo.UserInfoVO;
import com.wuxin.vo.WeChatLoginVO;

public interface UserService extends IService<UserEntity> {

    WeChatLoginVO wechatLogin(WeChatLoginDTO request);

    UserInfoVO getProfile(Long userId);

    void updateProfile(Long userId, UpdateUserProfileDTO request);

    UserInfoVO bindWechatPhone(Long userId, BindWechatPhoneDTO request);
}
