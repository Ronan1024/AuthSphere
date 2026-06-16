package com.authsphere.server.common.enums;

import lombok.Getter;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/10
 */
@Getter
public enum AuthMethodEnum {
    /**
     * 密码登录
     */
    PASSWORD_LOGIN("PASSWORD-LOGIN", "账号密码登录"),
    ;


    private final String code;
    private final String name;

    AuthMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


}
