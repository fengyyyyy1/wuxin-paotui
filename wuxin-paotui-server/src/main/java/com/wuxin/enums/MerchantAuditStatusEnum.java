package com.wuxin.enums;

public enum MerchantAuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核驳回");

    private final Integer code;

    private final String text;

    MerchantAuditStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static MerchantAuditStatusEnum of(Integer code) {
        for (MerchantAuditStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getTextByCode(Integer code) {
        MerchantAuditStatusEnum statusEnum = of(code);
        return statusEnum == null ? "未知状态" : statusEnum.getText();
    }
}
