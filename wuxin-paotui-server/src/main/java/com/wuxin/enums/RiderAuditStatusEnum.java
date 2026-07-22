package com.wuxin.enums;

public enum RiderAuditStatusEnum {

    PENDING(0, "审核中"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核未通过");

    private final Integer code;
    private final String text;

    RiderAuditStatusEnum(Integer code, String text) {
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
        for (RiderAuditStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.text;
            }
        }
        return "未知状态";
    }
}
