package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 登录页页面布局模式枚举。
 */
@Getter
public enum LayoutModeEnum {
    /**
     * 居中卡片。
     */
    CENTER_CARD("CENTER_CARD", "居中卡片"),

    /**
     * 左右分栏。
     */
    SPLIT_PANEL("SPLIT_PANEL", "左右分栏"),

    /**
     * 全屏沉浸。
     */
    FULLSCREEN("FULLSCREEN", "全屏沉浸");

    private final String code;
    private final String desc;

    LayoutModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 校验 code 是否为有效的枚举值。
     */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (LayoutModeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
