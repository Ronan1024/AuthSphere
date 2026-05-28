package com.authsphere.server.common.enums;

import lombok.Getter;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Getter
public enum ResponseCode {
    /**
     * 成功
     */
    SUCCESS("0000", "success"),
    /**
     * 失败
     */
    FAIL("-1", "fail"),
    /**
     * 参数错误
     */
    PARAM_ERROR("400", "参数错误"),
    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED("405", "请求方法不支持"),
    /**
     * 媒体类型不支持
     */
    UNSUPPORTED_MEDIA_TYPE("415", "媒体类型不支持"),
    /**
     * 系统异常
     */
    SYSTEM_ERROR("500", "系统异常"),
    ;

    private final String code;

    private final String message;

    private ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
