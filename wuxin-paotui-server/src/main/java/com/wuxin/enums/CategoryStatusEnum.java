package com.wuxin.enums;

public enum CategoryStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;

    private final String text;

    CategoryStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static CategoryStatusEnum of(Integer code) {
        for (CategoryStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        CategoryStatusEnum statusEnum = of(code);
        return statusEnum == null ? "未知状态" : statusEnum.getText();
    }
}
