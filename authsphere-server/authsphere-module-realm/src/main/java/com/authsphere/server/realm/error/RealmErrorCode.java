package com.authsphere.server.realm.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.REALM;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Getter
public enum RealmErrorCode implements BaseError {
    /**
     * 身份域编号已存在
     */
    REALM_CODE_EXISTS(REALM + "011", "身份域编号已存在"),
    /**
     * 身份域数据异常
     */
    REALM_DATA_ERROR(REALM + "012", "身份域数据异常"),
    ;

    private final String code;

    private final String message;

    RealmErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
