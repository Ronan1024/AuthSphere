package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 已存在会话处理方式枚举。
 */
@Getter
public enum ExistingSessionHandlerEnum {
    /**
     * 自动进入目标客户端。
     */
    AUTO_REDIRECT("auto_redirect", "自动进入目标客户端"),

    /**
     * 提示用户确认。
     */
    PROMPT("prompt", "提示用户确认");

    private final String code;
    private final String desc;

    ExistingSessionHandlerEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举对象，如果找不到则返回默认值。
     */
    public static ExistingSessionHandlerEnum fromCode(String code) {
        for (ExistingSessionHandlerEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return AUTO_REDIRECT; // 默认值
    }

    /**
     * 校验 code 是否为有效的枚举值。
     */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (ExistingSessionHandlerEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
