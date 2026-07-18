package com.wuxin.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer gender;
}
