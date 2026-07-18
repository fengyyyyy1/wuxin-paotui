package com.wuxin.gateway;

import com.wuxin.common.ResultCode;
import com.wuxin.config.MockWeChatLoginProperties;
import com.wuxin.config.WeChatMiniProgramProperties;
import com.wuxin.exception.BusinessException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeChatMiniProgramGatewayRouter {

    public static final String MOCK_GATEWAY = "MOCK";

    public static final String WECHAT_GATEWAY = "WECHAT";

    private final List<WeChatMiniProgramGateway> gateways;

    private final WeChatMiniProgramProperties weChatProperties;

    private final MockWeChatLoginProperties mockProperties;

    private final Environment environment;

    public WeChatMiniProgramGatewayRouter(
            List<WeChatMiniProgramGateway> gateways,
            WeChatMiniProgramProperties weChatProperties,
            MockWeChatLoginProperties mockProperties,
            Environment environment) {
        this.gateways = gateways;
        this.weChatProperties = weChatProperties;
        this.mockProperties = mockProperties;
        this.environment = environment;
    }

    public WeChatMiniProgramGateway getActiveGateway() {
        if (mockProperties.isEnabled()
                && environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_CONFIG_ERROR);
        }
        if (weChatProperties.isEnabled() && mockProperties.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_CONFIG_ERROR);
        }
        if (mockProperties.isEnabled()) {
            return getGateway(MOCK_GATEWAY);
        }
        if (weChatProperties.isEnabled()) {
            return getGateway(WECHAT_GATEWAY);
        }
        throw new BusinessException(ResultCode.WECHAT_LOGIN_DISABLED);
    }

    private WeChatMiniProgramGateway getGateway(String type) {
        return gateways.stream()
                .filter(gateway -> type.equals(gateway.getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.WECHAT_LOGIN_CONFIG_ERROR));
    }
}
