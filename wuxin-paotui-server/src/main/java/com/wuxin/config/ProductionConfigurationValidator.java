package com.wuxin.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductionConfigurationValidator {

    static final String DEVELOPMENT_JWT_SECRET =
            "wuxin-paotui-jwt-secret-key-for-development-2026";

    private final Environment environment;

    public ProductionConfigurationValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validate() {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            validateProd(environment);
        }
    }

    public static void validateProd(Environment environment) {
        List<String> missing = new ArrayList<>();
        require(environment, missing, "DB_URL");
        require(environment, missing, "DB_USERNAME");
        require(environment, missing, "DB_PASSWORD");
        require(environment, missing, "JWT_SECRET");
        require(environment, missing, "SERVER_PORT");

        String jwtSecret = environment.getProperty("JWT_SECRET");
        if (isBlank(jwtSecret) || DEVELOPMENT_JWT_SECRET.equals(jwtSecret)
                || jwtSecret.length() < 32) {
            missing.add("JWT_SECRET must be a production secret with at least 32 characters");
        }

        if (environment.getProperty("wuxin.wechat-mini-program.enabled", Boolean.class, false)) {
            require(environment, missing, "WECHAT_MINI_PROGRAM_APP_ID");
            require(environment, missing, "WECHAT_MINI_PROGRAM_APP_SECRET");
        }

        if (environment.getProperty("wuxin.wechat-pay.enabled", Boolean.class, false)) {
            require(environment, missing, "WECHAT_PAY_APP_ID");
            require(environment, missing, "WECHAT_PAY_MCH_ID");
            require(environment, missing, "WECHAT_PAY_MERCHANT_SERIAL_NUMBER");
            require(environment, missing, "WECHAT_PAY_PRIVATE_KEY_PATH");
            require(environment, missing, "WECHAT_PAY_API_V3_KEY");
            require(environment, missing, "WECHAT_PAY_NOTIFY_URL");
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Production configuration validation failed: "
                            + String.join(", ", missing));
        }
    }

    private static void require(
            Environment environment,
            List<String> missing,
            String name) {
        if (isBlank(environment.getProperty(name))) {
            missing.add(name);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
