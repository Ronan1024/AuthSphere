package com.authsphere.server.common.model;

import com.authsphere.server.common.enums.ResponseCode;

import static com.authsphere.server.common.enums.ResponseCode.SUCCESS;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
public class R<T> {

    private T data;

    private String code;

    private String message;

    private String version;


    public R(T data, String code, String message, String version) {
        this.data = data;
        this.code = code;
        this.message = message;
        this.version = version;
    }

    public static <T> R<T> success(T data) {
        return new R<>(data, SUCCESS.getCode(), SUCCESS.getMessage(), "v1");
    }

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> fail(ResponseCode responseCode) {
        return fail(responseCode.getCode(), responseCode.getMessage());
    }

    public static <T> R<T> fail(String message) {
        return fail(ResponseCode.FAIL.getCode(), message);
    }

    public static <T> R<T> fail(String code, String message) {
        return new R<>(null, code, message, "v1");
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
