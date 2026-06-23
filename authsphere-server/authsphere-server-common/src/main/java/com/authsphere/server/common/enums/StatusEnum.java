package com.authsphere.server.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
@Getter
public enum StatusEnum {
    /**
     * 正常
     */
    NORMAL(1, "正常"),
    /**
     * 禁用
     */
    DISABLED(2, "禁用");

    private final Integer code;

    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取当前状态反向状态
     */
    public static  Integer reverseStatus(Integer status) {
        return Objects.equals(status, NORMAL.code) ? DISABLED.getCode() : NORMAL.getCode();
    }

}
