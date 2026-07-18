package com.wuxin.vo;

import lombok.Data;

@Data
public class WeChatLoginVO {

    private String token;

    private UserInfoVO userInfo;

    private Boolean newUser;
}
