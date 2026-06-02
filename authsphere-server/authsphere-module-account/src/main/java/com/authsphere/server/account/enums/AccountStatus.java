package com.authsphere.server.account.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 账号状态：1 启用，2 锁定，3 禁用。
 */
@Getter
public enum AccountStatus {

    ENABLED(1, "启用"),
    LOCKED(2, "锁定"),
    DISABLED(3, "禁用");

    private final Integer code;

    private final String desc;

    AccountStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isEnabled(Integer status) {
        return Objects.equals(ENABLED.code, status);
    }

    public static boolean isLocked(Integer status) {
        return Objects.equals(LOCKED.code, status);
    }

    public static boolean isDisabled(Integer status) {
        return Objects.equals(DISABLED.code, status);
    }
}
