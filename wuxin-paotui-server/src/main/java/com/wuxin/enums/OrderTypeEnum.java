package com.wuxin.enums;

public enum OrderTypeEnum {

    ERRAND(0, "跑腿订单"),
    PRODUCT(1, "商品订单");

    private final Integer code;

    private final String text;

    OrderTypeEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static OrderTypeEnum of(Integer code) {
        for (OrderTypeEnum orderTypeEnum : values()) {
            if (orderTypeEnum.getCode().equals(code)) {
                return orderTypeEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        OrderTypeEnum orderTypeEnum = of(code);
        return orderTypeEnum == null ? "未知类型" : orderTypeEnum.getText();
    }
}
