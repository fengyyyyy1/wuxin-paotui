package com.wuxin.gateway;

import com.wuxin.gateway.model.WeChatSessionResult;

public interface WeChatMiniProgramGateway {

    String getType();

    WeChatSessionResult exchangeCode(String code);
}
