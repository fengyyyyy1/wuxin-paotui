package com.wuxin.enums;

public enum BusinessStatusEnum {

    CLOSED(0, "休息中"),
    OPEN(1, "营业中");

    private final Integer code;

    private final String text;

    BusinessStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static BusinessStatusEnum of(Integer code) {
        for (BusinessStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        BusinessStatusEnum statusEnum = of(code);
        return statusEnum == null ? "未知状态" : statusEnum.getText();
    }
}
