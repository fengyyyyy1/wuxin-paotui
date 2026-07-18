package com.wuxin.enums;

public enum OrderStatusEnum {

    WAITING_ACCEPT(0, "\u5f85\u63a5\u5355"),
    ACCEPTED(1, "\u5df2\u63a5\u5355"),
    DELIVERING(2, "\u914d\u9001\u4e2d"),
    WAITING_CONFIRM(3, "\u5f85\u786e\u8ba4\u6536\u8d27"),
    COMPLETED(4, "\u5df2\u5b8c\u6210"),
    CANCELLED(5, "\u5df2\u53d6\u6d88"),
    MERCHANT_PREPARING(6, "商家已接单，制作中"),
    WAITING_RIDER_ACCEPT(7, "已出餐，待骑手接单"),
    WAITING_REFUND(8, "已关闭，待退款");

    private final Integer code;

    private final String text;

    OrderStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static OrderStatusEnum of(Integer code) {
        for (OrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        OrderStatusEnum statusEnum = of(code);
        if (statusEnum == null) {
            return "\u672a\u77e5\u72b6\u6001";
        }
        return statusEnum.getText();
    }

    public static String getTextByCode(Integer code) {
        return getDescriptionByCode(code);
    }

    public static String getDescriptionByCode(
            Integer code,
            Integer orderType,
            Integer payStatus) {
        if (WAITING_ACCEPT.getCode().equals(code)
                && OrderTypeEnum.PRODUCT.getCode().equals(orderType)) {
            if (PaymentStatusEnum.UNPAID.getCode().equals(payStatus)) {
                return "待支付";
            }
            if (PaymentStatusEnum.PAID.getCode().equals(payStatus)) {
                return "待商家接单";
            }
        }
        return getDescriptionByCode(code);
    }
}
