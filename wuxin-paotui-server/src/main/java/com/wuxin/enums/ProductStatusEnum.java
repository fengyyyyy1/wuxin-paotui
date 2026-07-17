package com.wuxin.enums;

public enum ProductStatusEnum {

    OFF_SHELF(0, "已下架"),
    ON_SHELF(1, "已上架");

    private final Integer code;

    private final String text;

    ProductStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ProductStatusEnum of(Integer code) {
        for (ProductStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        ProductStatusEnum statusEnum = of(code);
        return statusEnum == null ? "未知状态" : statusEnum.getText();
    }
}
