package com.wuxin.common;

public enum ResultCode {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    ORDER_NOT_EXIST(404, "订单不存在"),
    RIDER_NOT_EXIST(404, "骑手不存在"),
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
    MOCK_PAYMENT_DISABLED(403, "模拟支付未启用"),
    PAYMENT_ORDER_NOT_EXIST(404, "支付单不存在"),
    PAYMENT_STATUS_INVALID(409, "当前支付状态不可操作"),
    PAYMENT_AMOUNT_INVALID(409, "支付金额无效或不一致"),
    PAYMENT_CREATE_FAILED(409, "支付单创建失败"),
    PAYMENT_NOT_SUPPORTED(409, "当前订单类型暂不支持支付"),
    USER_OPENID_MISSING(409, "当前用户缺少微信openid"),
    WECHAT_LOGIN_DISABLED(503, "微信登录未启用"),
    WECHAT_LOGIN_CONFIG_ERROR(500, "微信登录配置错误"),
    WECHAT_CODE_INVALID(400, "微信登录凭证无效"),
    WECHAT_LOGIN_FAILED(502, "微信登录失败，请稍后重试"),
    WECHAT_SERVICE_UNAVAILABLE(503, "微信登录服务暂时不可用"),
    WECHAT_RESPONSE_INVALID(502, "微信登录响应异常"),
    WECHAT_OPENID_MISSING(502, "微信登录未返回用户标识"),
    WECHAT_USER_CREATE_FAILED(409, "微信用户创建失败"),
    WECHAT_ACCOUNT_DISABLED(403, "当前账号已被禁用"),
    WECHAT_PHONE_BIND_DISABLED(503, "微信手机号绑定未启用"),
    WECHAT_PHONE_MOCK_FORBIDDEN(403, "生产环境禁止使用模拟微信手机号服务"),
    WECHAT_PHONE_CODE_INVALID(400, "微信手机号授权凭证无效"),
    PHONE_FORMAT_ERROR(400, "微信返回的手机号格式错误"),
    PHONE_ALREADY_BOUND(409, "手机号已绑定其他用户"),
    WECHAT_PHONE_SERVICE_ERROR(502, "微信手机号服务异常，请稍后重试"),
    MERCHANT_ALREADY_APPLIED(409, "当前用户已申请商家入驻"),
    MERCHANT_NOT_EXIST(404, "商家信息不存在"),
    MERCHANT_NOT_APPROVED(403, "商家尚未通过审核或已被禁用"),
    MERCHANT_ORDER_NOT_EXIST(404, "订单不存在或无权限"),
    MERCHANT_ORDER_UNPAID(409, "订单未支付，商家不可操作"),
    MERCHANT_ORDER_STATUS_ERROR(409, "当前订单状态不允许商家操作"),
    STORE_NOT_EXIST(404, "店铺不存在"),
    CATEGORY_NOT_EXIST(404, "商品分类不存在"),
    CATEGORY_NAME_EXIST(409, "商品分类名称已存在"),
    CATEGORY_HAS_PRODUCT(409, "分类下存在商品，不能删除"),
    CATEGORY_DISABLED(409, "商品分类已禁用"),
    PRODUCT_NOT_EXIST(404, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(409, "库存不足，商品不能上架"),
    CART_NOT_EXIST(404, "购物车不存在"),
    CART_STORE_CONFLICT(409, "购物车中已存在其他店铺商品"),
    PRODUCT_OFF_SHELF(409, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(409, "商品库存不足"),
    STORE_CLOSED(409, "店铺已停业"),
    STORE_DISABLED(409, "店铺已禁用"),
    CART_NO_SELECTED_PRODUCT(409, "购物车没有已选商品"),
    SETTLEMENT_CHANGED(409, "商品信息已变化，请重新结算"),
    ADDRESS_NOT_EXIST(404, "收货地址不存在"),
    ORDER_CREATE_FAILED(409, "商品订单创建失败"),
    BAD_REQUEST(400, "参数错误"),
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
