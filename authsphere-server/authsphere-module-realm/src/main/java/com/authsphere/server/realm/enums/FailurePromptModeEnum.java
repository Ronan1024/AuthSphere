package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 登录失败提示模式枚举。
 */
@Getter
public enum FailurePromptModeEnum {
    /**
     * 消息提示。
     */
    MESSAGE("message", "消息提示"),

    /**
     * 弹窗提示。
     */
    DIALOG("dialog", "弹窗提示"),

    /**
     * 结果页提示。
     */
    PAGE("page", "结果页提示");

    private final String code;
    private final String desc;

    FailurePromptModeEnum(String code, String desc) {
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
        for (FailurePromptModeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
