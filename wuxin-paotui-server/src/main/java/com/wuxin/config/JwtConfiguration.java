package com.wuxin.config;

import com.wuxin.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class JwtConfiguration {

    private final JwtProperties jwtProperties;

    public JwtConfiguration(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void configureJwtSecret() {
        JwtUtils.configureSecret(jwtProperties.getSecret());
    }
}
