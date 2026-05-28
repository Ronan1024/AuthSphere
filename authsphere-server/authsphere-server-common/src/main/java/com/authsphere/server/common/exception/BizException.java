package com.authsphere.server.common.exception;

import com.authsphere.server.common.enums.ResponseCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
public class BizException extends RuntimeException {

    private final String code;

    public BizException(String message) {
        super(message);
        this.code = ResponseCode.FAIL.getCode();
    }

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public BizException(BaseError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
