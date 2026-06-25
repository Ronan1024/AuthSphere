package com.authsphere.server.common.enums;

import lombok.Getter;

/**
 * 凭证类型。
 */
@Getter
public enum CredentialType {

    /**
     * 密码凭证。
     */
    PASSWORD(1, "PASSWORD"),

    /**
     * TOTP 动态口令凭证。
     */
    TOTP(2, "TOTP"),

    /**
     * Passkey/WebAuthn 凭证。
     */
    PASSKEY(3, "PASSKEY");

    private final Integer code;

    private final String desc;

    CredentialType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
