package com.wuxin.common;

public enum ResultCode {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    ORDER_NOT_EXIST(404, "订单不存在"),
    NOT_RIDER(403, "当前用户不是骑手"),
    ORDER_ALREADY_ACCEPTED(409, "订单已被其他骑手接单"),
    ORDER_STATUS_ERROR(409, "当前订单状态不可接单"),
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
