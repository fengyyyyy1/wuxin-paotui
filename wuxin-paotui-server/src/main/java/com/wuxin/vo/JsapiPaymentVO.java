package com.wuxin.vo;

import lombok.Data;

@Data
public class JsapiPaymentVO {

    private String paymentNo;

    private String timeStamp;

    private String nonceStr;

    private String packageValue;

    private String signType;

    private String paySign;
}
