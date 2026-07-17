package com.wuxin.enums;

public enum PaymentOrderStatusEnum {

    CREATED(0, "已创建"),
    WAITING_PAY(1, "待支付"),
    SUCCESS(2, "支付成功"),
    CLOSED(3, "已关闭"),
    FAILED(4, "支付失败");

    private final Integer code;

    private final String description;

    PaymentOrderStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentOrderStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentOrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        PaymentOrderStatusEnum status = of(code);
        return status == null ? "未知状态" : status.description;
    }
}
