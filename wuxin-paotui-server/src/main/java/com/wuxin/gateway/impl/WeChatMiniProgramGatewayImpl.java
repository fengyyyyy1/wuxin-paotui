package com.wuxin.gateway.impl;

import com.wuxin.common.ResultCode;
import com.wuxin.config.WeChatMiniProgramProperties;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.WeChatMiniProgramGateway;
import com.wuxin.gateway.WeChatMiniProgramGatewayRouter;
import com.wuxin.gateway.model.WeChatCodeSessionResponse;
import com.wuxin.gateway.model.WeChatSessionResult;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Set;

@Component
public class WeChatMiniProgramGatewayImpl implements WeChatMiniProgramGateway {

    private static final String WECHAT_API_BASE_URL = "https://api.weixin.qq.com";

    private static final Set<Integer> INVALID_CODE_ERRORS = Set.of(40029, 40163);

    private static final Set<Integer> CONFIG_ERRORS = Set.of(40013, 40125);

    private final WeChatMiniProgramProperties properties;

    private final RestClient restClient;

    public WeChatMiniProgramGatewayImpl(
            WeChatMiniProgramProperties properties,
            RestClient.Builder restClientBuilder) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = restClientBuilder
                .baseUrl(WECHAT_API_BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String getType() {
        return WeChatMiniProgramGatewayRouter.WECHAT_GATEWAY;
    }

    @Override
    public WeChatSessionResult exchangeCode(String code) {
        validateConfiguration();
        WeChatCodeSessionResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sns/jscode2session")
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getAppSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(WeChatCodeSessionResponse.class);
        } catch (ResourceAccessException exception) {
            throw new BusinessException(ResultCode.WECHAT_SERVICE_UNAVAILABLE);
        } catch (RestClientResponseException exception) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        } catch (RestClientException exception) {
            throw new BusinessException(ResultCode.WECHAT_RESPONSE_INVALID);
        }

        validateResponse(response);
        return WeChatSessionResult.builder()
                .openId(response.getOpenId())
                .unionId(response.getUnionId())
                .sessionKey(response.getSessionKey())
                .build();
    }

    private void validateConfiguration() {
        if (isBlank(properties.getAppId()) || isBlank(properties.getAppSecret())) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_CONFIG_ERROR);
        }
    }

    private void validateResponse(WeChatCodeSessionResponse response) {
        if (response == null) {
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }
        Integer errorCode = response.getErrorCode();
        if (errorCode != null && errorCode != 0) {
            if (INVALID_CODE_ERRORS.contains(errorCode)) {
                throw new BusinessException(ResultCode.WECHAT_CODE_INVALID);
            }
            if (CONFIG_ERRORS.contains(errorCode)) {
                throw new BusinessException(ResultCode.WECHAT_LOGIN_CONFIG_ERROR);
            }
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }
        if (isBlank(response.getOpenId())) {
            throw new BusinessException(ResultCode.WECHAT_OPENID_MISSING);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
