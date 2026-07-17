package com.wuxin.enums;

public enum RankingTypeEnum {

    TODAY("today"),
    WEEK("week"),
    MONTH("month"),
    TOTAL("total");

    private final String value;

    RankingTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RankingTypeEnum of(String value) {
        if (value == null) {
            return null;
        }
        for (RankingTypeEnum type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
