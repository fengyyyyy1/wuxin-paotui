package com.wuxin.gateway.impl;

import com.wuxin.common.ResultCode;
import com.wuxin.config.MockWeChatPhoneProperties;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.model.WeChatPhoneResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockWeChatPhoneGatewayTest {

    @Test
    void shouldReturnFixedPhoneMappings() {
        MockWeChatPhoneProperties properties = new MockWeChatPhoneProperties();
        properties.setEnabled(true);
        MockWeChatPhoneGateway gateway = new MockWeChatPhoneGateway(properties);

        WeChatPhoneResult first =
                gateway.exchangeCode("mock-phone-code-13800000003");
        WeChatPhoneResult second =
                gateway.exchangeCode("mock-phone-code-13900000003");

        assertThat(first.getPurePhoneNumber()).isEqualTo("13800000003");
        assertThat(first.getPhoneNumber()).isEqualTo("+8613800000003");
        assertThat(first.getCountryCode()).isEqualTo("86");
        assertThat(second.getPurePhoneNumber()).isEqualTo("13900000003");
    }

    @Test
    void shouldRejectInvalidCodeWithBusinessError() {
        MockWeChatPhoneProperties properties = new MockWeChatPhoneProperties();
        properties.setEnabled(true);
        MockWeChatPhoneGateway gateway = new MockWeChatPhoneGateway(properties);

        assertThatThrownBy(() -> gateway.exchangeCode("mock-phone-code-invalid"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.WECHAT_PHONE_CODE_INVALID));
    }

    @Test
    void shouldRejectCallsWhenMockIsDisabled() {
        MockWeChatPhoneGateway gateway =
                new MockWeChatPhoneGateway(new MockWeChatPhoneProperties());

        assertThatThrownBy(
                () -> gateway.exchangeCode("mock-phone-code-13800000003"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.WECHAT_PHONE_BIND_DISABLED));
    }
}
