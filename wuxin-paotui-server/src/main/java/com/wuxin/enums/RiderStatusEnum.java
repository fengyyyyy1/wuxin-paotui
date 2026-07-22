package com.wuxin.enums;

public enum RiderStatusEnum {

    NOT_ENABLED(0, "未开通"),
    ENABLED(1, "正常"),
    DISABLED(2, "已禁用");

    private final Integer code;
    private final String text;

    RiderStatusEnum(Integer code, String text) {
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
        for (RiderStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.text;
            }
        }
        return "未知状态";
    }
}
