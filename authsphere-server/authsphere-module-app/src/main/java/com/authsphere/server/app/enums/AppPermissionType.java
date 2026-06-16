package com.authsphere.server.app.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 应用权限资源类型。
 */
@Getter
public enum AppPermissionType {
    /**
     * 按钮或页面操作权限。
     */
    BUTTON(1),
    /**
     * 接口访问权限。
     */
    API(2),
    /**
     * 数据权限，预留给后续 DataScope 组合使用。
     */
    DATA(3);

    /**
     * 数据库存储值。
     */
    private final Integer code;

    AppPermissionType(Integer code) {
        this.code = code;
    }

    public static boolean valid(Integer code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equals(code));
    }
}
