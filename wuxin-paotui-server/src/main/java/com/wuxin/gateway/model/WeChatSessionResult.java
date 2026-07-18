package com.wuxin.gateway.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeChatSessionResult {

    private final String openId;

    private final String unionId;

    private final String sessionKey;
}
