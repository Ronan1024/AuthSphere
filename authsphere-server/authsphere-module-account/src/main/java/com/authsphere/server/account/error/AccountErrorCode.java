package com.authsphere.server.account.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.ACCOUNT;

/**
 * 账号错误码。
 *
 * @author L.J.Ran
 */
@Getter
public enum AccountErrorCode implements BaseError {
    /**
     * 用户名已存在
     */
    ACCOUNT_USERNAME_EXISTS(ACCOUNT + "011", "用户名已存在"),
    /**
     * 账号数据异常
     */
    ACCOUNT_DATA_ERROR(ACCOUNT + "012", "账号数据异常"),
    /**
     * 账号状态不允许当前操作
     */
    ACCOUNT_STATUS_INVALID(ACCOUNT + "013", "账号状态不允许当前操作"),
    /**
     * 身份域数据异常
     */
    ACCOUNT_REALM_DATA_ERROR(ACCOUNT + "014", "身份域数据异常"),
    ;

    private final String code;

    private final String message;

    AccountErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
