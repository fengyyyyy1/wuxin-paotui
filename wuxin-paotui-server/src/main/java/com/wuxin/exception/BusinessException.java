package com.wuxin.exception;

import com.wuxin.common.ResultCode;

public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.resultCode = null;
        this.code = code;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public Integer getCode() {
        return code;
    }
}
