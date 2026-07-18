package com.wuxin.gateway.impl;

import com.wuxin.common.ResultCode;
import com.wuxin.config.MockWeChatPhoneProperties;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.WeChatPhoneGateway;
import com.wuxin.gateway.WeChatPhoneGatewayRouter;
import com.wuxin.gateway.model.WeChatPhoneResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockWeChatPhoneGateway implements WeChatPhoneGateway {

    private static final Map<String, String> CODE_PHONE_MAPPING = Map.of(
            "mock-phone-code-13800000003", "13800000003",
            "mock-phone-code-13900000003", "13900000003");

    private final MockWeChatPhoneProperties properties;

    public MockWeChatPhoneGateway(MockWeChatPhoneProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getType() {
        return WeChatPhoneGatewayRouter.MOCK_GATEWAY;
    }

    @Override
    public WeChatPhoneResult exchangeCode(String code) {
        if (!properties.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PHONE_BIND_DISABLED);
        }
        String phone = CODE_PHONE_MAPPING.get(code);
        if (phone == null) {
            throw new BusinessException(ResultCode.WECHAT_PHONE_CODE_INVALID);
        }
        return WeChatPhoneResult.builder()
                .phoneNumber("+86" + phone)
                .purePhoneNumber(phone)
                .countryCode("86")
                .build();
    }
}
