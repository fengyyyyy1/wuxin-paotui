package com.wuxin.enums;

public enum OrderStatusEnum {

    WAITING_ACCEPT(0, "\u5f85\u63a5\u5355"),
    ACCEPTED(1, "\u5df2\u63a5\u5355"),
    DELIVERING(2, "\u914d\u9001\u4e2d"),
    COMPLETED(3, "\u5df2\u5b8c\u6210"),
    CANCELLED(4, "\u5df2\u53d6\u6d88");

    private final Integer code;

    private final String text;

    OrderStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static String getTextByCode(Integer code) {
        for (OrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getText();
            }
        }
        return "\u672a\u77e5\u72b6\u6001";
    }
}
