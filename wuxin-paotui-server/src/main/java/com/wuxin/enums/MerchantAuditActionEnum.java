package com.wuxin.enums;

public enum MerchantAuditActionEnum {

    APPROVE("APPROVE"),
    REJECT("REJECT"),
    ENABLE("ENABLE"),
    DISABLE("DISABLE");

    private final String code;

    MerchantAuditActionEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
