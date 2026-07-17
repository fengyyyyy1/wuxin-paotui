package com.wuxin.common;

public enum ResultCode {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    ORDER_NOT_EXIST(404, "订单不存在"),
    NOT_RIDER(403, "当前用户不是骑手"),
    ORDER_ALREADY_ACCEPTED(409, "订单已被其他骑手接单"),
    ORDER_STATUS_ERROR(409, "当前订单状态不可接单"),
    ORDER_STATUS_CANNOT_CANCEL(409, "当前订单状态不可取消"),
    ORDER_STATUS_CANNOT_GIVE_UP(409, "当前订单状态不可放弃"),
    ORDER_STATUS_CANNOT_COMMENT(409, "当前订单状态不可评价"),
    ORDER_ALREADY_COMMENTED(409, "订单已评价"),
    ORDER_ALREADY_PAID(409, "订单已支付"),
    ORDER_STATUS_CANNOT_PAY(409, "当前订单状态不可支付"),
    ORDER_NOT_PAID(409, "订单未支付"),
    MERCHANT_ALREADY_APPLIED(409, "当前用户已申请商家入驻"),
    MERCHANT_NOT_EXIST(404, "商家信息不存在"),
    MERCHANT_NOT_APPROVED(403, "商家尚未通过审核或已被禁用"),
    STORE_NOT_EXIST(404, "店铺不存在"),
    CATEGORY_NOT_EXIST(404, "商品分类不存在"),
    CATEGORY_NAME_EXIST(409, "商品分类名称已存在"),
    CATEGORY_HAS_PRODUCT(409, "分类下存在商品，不能删除"),
    CATEGORY_DISABLED(409, "商品分类已禁用"),
    PRODUCT_NOT_EXIST(404, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(409, "库存不足，商品不能上架"),
    USERNAME_EXIST(1001, "用户名已存在"),
    USER_NOT_EXIST(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PARAM_ERROR(1004, "参数错误");

    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
