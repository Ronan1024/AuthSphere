package com.authsphere.server.role.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.ROLE;

/**
 * 角色授权模块错误码。
 */
@Getter
public enum RoleErrorCode implements BaseError {
    ROLE_DATA_ERROR(ROLE + "001", "角色数据异常"),
    ROLE_CODE_EXISTS(ROLE + "002", "角色编码已存在"),
    ROLE_CLIENT_ERROR(ROLE + "003", "客户端数据异常或已禁用"),
    ROLE_RESOURCE_SCOPE_ERROR(ROLE + "004", "角色授权资源必须属于同一客户端"),
    ROLE_ACCOUNT_SCOPE_ERROR(ROLE + "005", "账号角色授权必须属于同一客户端"),
    ROLE_DISABLED_DENIED(ROLE + "006", "角色已禁用，不能执行当前操作"),
    ROLE_DELETE_DENIED(ROLE + "007", "角色启用中或仍绑定账号，不能删除"),
    ROLE_SYSTEM_DELETE_DENIED(ROLE + "008", "系统角色不允许删除"),
    ACCOUNT_ROLE_ACCOUNT_ERROR(ROLE + "009", "账号数据异常"),
    ACCOUNT_ROLE_SUBJECT_ERROR(ROLE + "010", "主体数据异常");

    private final String code;

    private final String message;

    RoleErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
