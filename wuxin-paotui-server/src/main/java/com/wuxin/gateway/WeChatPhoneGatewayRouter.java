package com.wuxin.gateway;

import com.wuxin.common.ResultCode;
import com.wuxin.config.MockWeChatPhoneProperties;
import com.wuxin.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WeChatPhoneGatewayRouter {

    public static final String MOCK_GATEWAY = "MOCK";

    private final List<WeChatPhoneGateway> gateways;

    private final MockWeChatPhoneProperties mockProperties;

    private final Environment environment;

    public WeChatPhoneGatewayRouter(
            List<WeChatPhoneGateway> gateways,
            MockWeChatPhoneProperties mockProperties,
            Environment environment) {
        this.gateways = gateways;
        this.mockProperties = mockProperties;
        this.environment = environment;
    }

    public WeChatPhoneGateway getActiveGateway() {
        if (!mockProperties.isEnabled()) {
            throw new BusinessException(ResultCode.WECHAT_PHONE_BIND_DISABLED);
        }
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            log.error("Security protection blocked Mock WeChat phone gateway in prod profile");
            throw new BusinessException(ResultCode.WECHAT_PHONE_MOCK_FORBIDDEN);
        }
        return gateways.stream()
                .filter(gateway -> MOCK_GATEWAY.equals(gateway.getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ResultCode.WECHAT_PHONE_SERVICE_ERROR));
    }
}
