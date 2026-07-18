package com.wuxin.gateway.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeChatPhoneResult {

    private final String phoneNumber;

    private final String purePhoneNumber;

    private final String countryCode;
}
