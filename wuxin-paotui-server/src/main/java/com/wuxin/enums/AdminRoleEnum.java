package com.wuxin.enums;

public enum AdminRoleEnum {

    ADMIN("ADMIN");

    private final String code;

    AdminRoleEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
