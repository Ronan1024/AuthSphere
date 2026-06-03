package com.authsphere.server.app.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.APP;

/**
 * 应用模块错误码。
 */
@Getter
public enum AppErrorCode implements BaseError {
    APP_DATA_ERROR(APP + "001", "应用数据异常"),
    APP_CODE_EXISTS(APP + "002", "应用编码已存在"),
    APP_DISABLED_DENIED(APP + "003", "应用已禁用，不能执行当前操作"),
    APP_HAS_ENABLED_INSTANCE(APP + "004", "应用存在启用中的实例，不能禁用"),
    APP_MENU_DATA_ERROR(APP + "005", "应用菜单数据异常"),
    APP_MENU_CODE_EXISTS(APP + "006", "应用菜单编码已存在"),
    APP_MENU_PARENT_ERROR(APP + "007", "父级菜单必须属于同一应用"),
    APP_PERMISSION_DATA_ERROR(APP + "008", "应用权限数据异常"),
    APP_PERMISSION_CODE_EXISTS(APP + "009", "应用权限编码已存在"),
    APP_PERMISSION_MENU_ERROR(APP + "010", "权限所属菜单必须属于同一应用"),
    APP_INSTANCE_DATA_ERROR(APP + "011", "应用实例数据异常"),
    APP_INSTANCE_CODE_EXISTS(APP + "012", "应用实例编码已存在"),
    APP_INSTANCE_SUBJECT_APP_EXISTS(APP + "013", "该主体已开通过同一应用"),
    APP_INSTANCE_DISABLED_DENIED(APP + "014", "应用实例已禁用，不能执行当前操作"),
    APP_INSTANCE_RESOURCE_ERROR(APP + "015", "实例资源必须属于实例对应应用"),
    APP_CLIENT_DATA_ERROR(APP + "016", "应用客户端数据异常"),
    APP_CLIENT_ID_EXISTS(APP + "017", "应用端编码已存在"),
    APP_CLIENT_SECRET_REQUIRED(APP + "018", "当前客户端类型必须配置 client_secret"),
    APP_CLIENT_TOKEN_TTL_ERROR(APP + "019", "Token 有效期必须大于 0"),
    APP_CLIENT_REDIRECT_URI_ERROR(APP + "020", "回调地址格式不合法"),
    APP_CLIENT_EXTERNAL_CONFIG_DATA_ERROR(APP + "021", "客户端外部平台配置数据异常"),
    APP_CLIENT_EXTERNAL_CONFIG_EXISTS(APP + "022", "同一客户端下外部平台配置已存在"),
    APP_REALM_DATA_ERROR(APP + "023", "身份域数据异常"),
    APP_SUBJECT_DATA_ERROR(APP + "024", "主体数据异常或已禁用"),
    APP_CLIENT_DISABLED_DENIED(APP + "025", "应用端已禁用，不能执行当前操作"),
    APP_CLIENT_INSTANCE_DATA_ERROR(APP + "026", "应用端实例数据异常"),
    APP_CLIENT_INSTANCE_CODE_EXISTS(APP + "027", "应用端实例编码已存在"),
    APP_CLIENT_INSTANCE_EXISTS(APP + "028", "当前应用实例已启用同一应用端"),
    APP_CLIENT_INSTANCE_APP_MISMATCH(APP + "029", "应用端必须属于应用实例对应应用"),
    ROLE_DATA_ERROR(APP + "030", "角色数据异常"),
    ROLE_CODE_EXISTS(APP + "031", "角色编码已存在"),
    ROLE_RESOURCE_SCOPE_ERROR(APP + "032", "角色授权资源必须属于同一应用端"),
    MEMBER_ROLE_MEMBER_ERROR(APP + "033", "主体成员数据异常或状态不可授权"),
    MEMBER_ROLE_SCOPE_ERROR(APP + "034", "成员角色授权必须属于同一应用端实例");

    private final String code;

    private final String message;

    AppErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
