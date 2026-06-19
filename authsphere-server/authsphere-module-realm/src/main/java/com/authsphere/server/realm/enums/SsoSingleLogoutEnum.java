package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 单点退出策略枚举。
 */
@Getter
public enum SsoSingleLogoutEnum {
    /**
     * 统一退出同域客户端。
     */
    ENABLED("enabled", "统一退出同域客户端"),

    /**
     * 仅退出当前客户端。
     */
    CURRENT_ONLY("current_only", "仅退出当前客户端");

    private final String code;
    private final String desc;

    SsoSingleLogoutEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举对象，如果找不到则返回默认值。
     */
    public static SsoSingleLogoutEnum fromCode(String code) {
        for (SsoSingleLogoutEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return ENABLED; // 默认值
    }

    /**
     * 校验 code 是否为有效的枚举值。
     */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (SsoSingleLogoutEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
