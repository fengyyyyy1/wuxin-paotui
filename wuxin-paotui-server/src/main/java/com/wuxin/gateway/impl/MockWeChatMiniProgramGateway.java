package com.wuxin.gateway.impl;

import com.wuxin.common.ResultCode;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.WeChatMiniProgramGateway;
import com.wuxin.gateway.WeChatMiniProgramGatewayRouter;
import com.wuxin.gateway.model.WeChatSessionResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockWeChatMiniProgramGateway implements WeChatMiniProgramGateway {

    private static final Map<String, String> CODE_OPENID_MAPPING = Map.of(
            "mock-code-test001", "mock_openid_test001",
            "mock-code-new-user", "mock_openid_new_user",
            "mock-code-new-user-repeat", "mock_openid_new_user");

    @Override
    public String getType() {
        return WeChatMiniProgramGatewayRouter.MOCK_GATEWAY;
    }

    @Override
    public WeChatSessionResult exchangeCode(String code) {
        String openId = CODE_OPENID_MAPPING.get(code);
        if (openId == null) {
            throw new BusinessException(ResultCode.WECHAT_CODE_INVALID);
        }
        return WeChatSessionResult.builder()
                .openId(openId)
                .sessionKey("mock_session_key_local_only")
                .build();
    }
}
