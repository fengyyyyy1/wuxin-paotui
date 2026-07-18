package com.wuxin.gateway;

import com.wuxin.gateway.model.WeChatPhoneResult;

public interface WeChatPhoneGateway {

    String getType();

    WeChatPhoneResult exchangeCode(String code);
}
