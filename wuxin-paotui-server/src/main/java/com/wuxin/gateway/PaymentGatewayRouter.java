package com.wuxin.gateway;

import com.wuxin.common.ResultCode;
import com.wuxin.config.MockPaymentProperties;
import com.wuxin.config.WeChatPayProperties;
import com.wuxin.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentGatewayRouter {

    public static final String MOCK_GATEWAY = "MOCK";

    public static final String WECHAT_GATEWAY = "WECHAT";

    private final List<PaymentGateway> gateways;

    private final WeChatPayProperties weChatPayProperties;

    private final MockPaymentProperties mockPaymentProperties;

    public PaymentGatewayRouter(
            List<PaymentGateway> gateways,
            WeChatPayProperties weChatPayProperties,
            MockPaymentProperties mockPaymentProperties) {
        this.gateways = gateways;
        this.weChatPayProperties = weChatPayProperties;
        this.mockPaymentProperties = mockPaymentProperties;
    }

    public PaymentGateway getActiveGateway() {
        if (weChatPayProperties.isEnabled()) {
            return getGateway(WECHAT_GATEWAY);
        }
        if (mockPaymentProperties.isEnabled()) {
            return getGateway(MOCK_GATEWAY);
        }
        throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED, "支付网关未启用");
    }

    public PaymentGateway getGateway(String type) {
        return gateways.stream()
                .filter(gateway -> gateway.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ResultCode.PAYMENT_CREATE_FAILED,
                        WECHAT_GATEWAY.equals(type)
                                ? "真实微信支付网关尚未实现"
                                : "模拟支付网关未启用"));
    }
}
