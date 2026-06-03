package com.authsphere.server.account.enums;

import lombok.Getter;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Getter
public enum CredentialType {

    /**
     * password
     */
    PASSWORD(1, "PASSWORD"),
    /**
     * TOTP
     */
    TOTP(2, "TOTP"),
    /**
     * passkey
     */
    PASSKEY(3, "PASSKEY"),

    ;

    private final Integer code;

    private final String desc;

    CredentialType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
