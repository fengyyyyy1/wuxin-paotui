package com.wuxin.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeChatCodeSessionResponse {

    @JsonProperty("openid")
    private String openId;

    @JsonProperty("unionid")
    private String unionId;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("errcode")
    private Integer errorCode;

    @JsonProperty("errmsg")
    private String errorMessage;
}
