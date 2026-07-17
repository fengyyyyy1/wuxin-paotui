package com.wuxin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wuxin.wechat-pay")
public class WeChatPayProperties {

    private boolean enabled;

    private String appId;

    private String mchId;

    private String merchantSerialNumber;

    private String privateKeyPath;

    private String apiV3Key;

    private String notifyUrl;
}
