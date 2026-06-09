package com.authsphere.server.realm.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 认证方式状态。
 */
@Getter
public enum AuthMethodStatus {

    ENABLED(1, "启用"),
    PENDING_CONFIGURATION(2, "待配置"),
    DISABLED(3, "禁用");

    private final Integer code;
    private final String description;

    AuthMethodStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断状态编码是否有效。
     *
     * @param code 状态编码
     * @return 是否属于认证方式状态
     */
    public static boolean supports(Integer code) {
        return Arrays.stream(values()).anyMatch(status -> status.code.equals(code));
    }
}
