package com.authsphere.server.realm.enums;

import lombok.Getter;

/**
 * 密码复杂度枚举。
 */
@Getter
public enum PasswordComplexityEnum {
    LETTERS_DIGITS("letters_digits", "数字 + 字母"),
    LETTERS_DIGITS_SYMBOLS("letters_digits_symbols", "数字 + 大小写 + 特殊字符"),
    NONE("none", "不限制");

    private final String code;
    private final String desc;

    PasswordComplexityEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (PasswordComplexityEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
