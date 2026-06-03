package com.authsphere.server.app.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 应用客户端类型。
 */
@Getter
public enum AppClientType {
    /**
     * Web 管理后台客户端。
     */
    ADMIN_WEB(1),
    /**
     * 商户管理端。
     */
    MERCHANT_WEB(2),
    /**
     * 小程序客户端。
     */
    MINI_PROGRAM(3),
    /**
     * H5/Web 端。
     */
    H5(4),
    /**
     * 开放 API 调用客户端。
     */
    OPEN_API(5),
    /**
     * 服务间调用客户端。
     */
    SERVICE(6);

    /**
     * 数据库存储值。
     */
    private final Integer code;

    AppClientType(Integer code) {
        this.code = code;
    }

    public static AppClientType of(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
