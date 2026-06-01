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
public enum TypeCategoryErrorCode implements BaseError {
    /**
     * 当前类型已存在身份域绑定
     */
    TYPE_CATEGORY_BIND_REALM(TYPE_CATEGORY + "011", "当前类型已存在身份域绑定"),
    ;


    private String code;
    private String message;

    TypeCategoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
