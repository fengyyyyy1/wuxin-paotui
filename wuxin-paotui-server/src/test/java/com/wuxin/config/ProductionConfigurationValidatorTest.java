package com.wuxin.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTest {

    @Test
    void prodShouldFailWhenRequiredVariablesMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("wuxin.wechat-pay.enabled", "false")
                .withProperty("wuxin.wechat-mini-program.enabled", "false");

        assertThatThrownBy(() -> ProductionConfigurationValidator.validateProd(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_URL")
                .hasMessageContaining("DB_PASSWORD")
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void mockModeShouldNotRequireWechatPayVariables() {
        MockEnvironment environment = baseProductionEnvironment()
                .withProperty("wuxin.wechat-pay.enabled", "false")
                .withProperty("wuxin.wechat-mini-program.enabled", "false");

        ProductionConfigurationValidator.validateProd(environment);
    }

    @Test
    void enabledWechatPayShouldRequirePaymentVariables() {
        MockEnvironment environment = baseProductionEnvironment()
                .withProperty("wuxin.wechat-pay.enabled", "true")
                .withProperty("wuxin.wechat-mini-program.enabled", "false");

        assertThatThrownBy(() -> ProductionConfigurationValidator.validateProd(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WECHAT_PAY_APP_ID")
                .hasMessageContaining("WECHAT_PAY_NOTIFY_URL");
    }

    private MockEnvironment baseProductionEnvironment() {
        return new MockEnvironment()
                .withProperty("DB_URL", "jdbc:mysql://db.example.internal:3306/wuxin_paotui")
                .withProperty("DB_USERNAME", "wuxin_app")
                .withProperty("DB_PASSWORD", "replace-with-strong-password")
                .withProperty("JWT_SECRET", "replace-with-at-least-32-chars-secret")
                .withProperty("SERVER_PORT", "8080");
    }
}
