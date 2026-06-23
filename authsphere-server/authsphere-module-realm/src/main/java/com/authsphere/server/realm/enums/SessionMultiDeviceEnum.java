package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 多端会话策略枚举。
 */
@Getter
public enum SessionMultiDeviceEnum {
    ALLOW("allow", "允许多端同时在线"),
    LIMIT("limit", "限制最大登录设备数");

    private final String code;
    private final String desc;

    SessionMultiDeviceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (SessionMultiDeviceEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
