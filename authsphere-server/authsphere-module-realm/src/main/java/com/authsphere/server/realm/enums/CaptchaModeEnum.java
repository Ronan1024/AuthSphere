package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 图形验证码模式枚举。
 */
@Getter
public enum CaptchaModeEnum {
    /**
     * 不启用图形验证码。
     */
    NONE("none", "不启用"),

    /**
     * 失败阈值后启用图形验证码。
     */
    THRESHOLD("threshold", "失败 3 次后启用"),

    /**
     * 每次登录都启用图形验证码。
     */
    ALWAYS("always", "每次登录都启用");

    private final String code;
    private final String desc;

    CaptchaModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举对象，找不到时返回默认值。
     */
    public static CaptchaModeEnum fromCode(String code) {
        if (code == null) {
            return NONE;
        }
        for (CaptchaModeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return NONE;
    }

    /**
     * 校验 code 是否为有效的枚举值。
     */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (CaptchaModeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
