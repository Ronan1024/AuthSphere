package com.authsphere.server.app.enums;

import java.util.Arrays;

/**
 * 应用实例开通方式。
 */
public enum AppOpenMode {
    /**
     * 平台管理员开通。
     */
    PLATFORM,
    /**
     * 主体自助开通。
     */
    SELF;

    /**
     * 判断开通方式是否合法。
     */
    public static boolean valid(String value) {
        return Arrays.stream(values()).anyMatch(item -> item.name().equals(value));
    }
}
