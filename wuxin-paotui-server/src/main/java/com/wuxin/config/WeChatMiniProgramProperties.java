package com.wuxin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wuxin.wechat-mini-program")
public class WeChatMiniProgramProperties {

    private boolean enabled;

    private String appId;

    private String appSecret;
}
