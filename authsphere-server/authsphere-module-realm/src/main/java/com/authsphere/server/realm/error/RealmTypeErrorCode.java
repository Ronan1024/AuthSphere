package com.authsphere.server.realm.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.REALM;
import static com.authsphere.server.common.constant.BaseErrorCode.TYPE_CATEGORY;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/1
 */

@Getter
public enum RealmTypeErrorCode implements BaseError {
    /**
     * 当前类型已存在身份域绑定
     */
    TYPE_CATEGORY_BIND_REALM(TYPE_CATEGORY + "011", "当前类型已存在身份域绑定"),

    /**
     * 类型为系统内置类型，不可删除
     */
    TYPE_CATEGORY_SYSTEM_BUILTIN(TYPE_CATEGORY + "012", "类型为系统内置类型，不可删除"),
    ;


    private final String code;
    private final String message;

    RealmTypeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
