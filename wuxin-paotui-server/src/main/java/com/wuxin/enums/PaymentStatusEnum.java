package com.wuxin.enums;

public enum PaymentStatusEnum {

    UNPAID(0, "未支付"),
    PAID(1, "已支付");

    private final Integer code;

    private final String text;

    PaymentStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static PaymentStatusEnum of(Integer code) {
        for (PaymentStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        PaymentStatusEnum statusEnum = of(code);
        return statusEnum == null ? "未知状态" : statusEnum.getText();
    }
}
