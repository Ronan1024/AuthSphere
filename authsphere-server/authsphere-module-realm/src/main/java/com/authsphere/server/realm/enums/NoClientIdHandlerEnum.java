package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 无 client_id 时的处理方式枚举。
 */
@Getter
public enum NoClientIdHandlerEnum {
    /**
     * 展示应用选择页。
     */
    SHOW_APP_LIST("show_app_list", "展示应用选择页"),

    /**
     * 返回错误提示。
     */
    SHOW_ERROR("show_error", "返回错误提示");

    private final String code;
    private final String desc;

    NoClientIdHandlerEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举对象，如果找不到则返回默认值。
     */
    public static NoClientIdHandlerEnum fromCode(String code) {
        for (NoClientIdHandlerEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return SHOW_APP_LIST; // 默认值
    }

    /**
     * 校验 code 是否为有效的枚举值。
     */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (NoClientIdHandlerEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
