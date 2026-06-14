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
    /**
     * 密码策略数据异常
     */
    PASSWORD_POLICY_DATA_ERROR(REALM + "013", "密码策略数据异常"),
    /**
     * 密码策略参数异常
     */
    PASSWORD_POLICY_PARAM_ERROR(REALM + "014", "密码策略参数异常"),
    /**
     * 密码策略编码已存在
     */
    PASSWORD_POLICY_CODE_EXISTS(REALM + "015", "密码策略编码已存在"),
    /**
     * MFA 策略数据异常
     */
    MFA_POLICY_DATA_ERROR(REALM + "016", "MFA 策略数据异常"),
    /**
     * MFA 策略编码已存在
     */
    MFA_POLICY_CODE_EXISTS(REALM + "017", "MFA 策略编码已存在"),
    /**
     * 账号唯一性规则数据异常
     */
    UNIQUE_POLICY_DATA_ERROR(REALM + "018", "账号唯一性规则数据异常"),
    /**
     * 账号唯一性规则编码已存在
     */
    UNIQUE_POLICY_CODE_EXISTS(REALM + "019", "账号唯一性规则编码已存在"),
    /**
     * 类型分类数据异常
     */
    TYPE_CATEGORY_DATA_ERROR(REALM + "020", "类型分类数据异常"),
    /**
     * 类型分类编码已存在
     */
    TYPE_CATEGORY_CODE_EXISTS(REALM + "021", "类型分类编码已存在"),
    /**
     * 类型分类不可编辑
     */
    TYPE_CATEGORY_NOT_EDITABLE(REALM + "022", "类型分类不可编辑"),
    /**
     * 登录页数据不存在
     */
    LOGIN_PAGE_DATA_ERROR(REALM + "023", "登录页数据不存在"),
    /**
     * 登录页编码已存在
     */
    LOGIN_PAGE_CODE_EXISTS(REALM + "024", "登录页编码已存在"),
    /**
     * 默认登录方式不在已选登录方式中
     */
    LOGIN_PAGE_AUTH_METHOD_ERROR(REALM + "025", "默认登录方式必须属于已选登录方式"),
    /**
     * 登录页状态异常
     */
    LOGIN_PAGE_STATUS_ERROR(REALM + "026", "登录页状态异常"),
    /**
     * 登录页已被身份域引用
     */
    LOGIN_PAGE_REALM_REFERENCED(REALM + "027", "登录页已被身份域引用，请先解除绑定"),
    /**
     * 登录页已被客户端引用
     */
    LOGIN_PAGE_CLIENT_REFERENCED(REALM + "028", "登录页已被客户端引用，请先解除绑定"),
    /**
     * 系统内置登录页不能删除
     */
    LOGIN_PAGE_SYSTEM_BUILTIN(REALM + "029", "系统内置登录页不能删除"),
    /**
     * 禁用登录页不能被绑定
     */
    LOGIN_PAGE_DISABLED(REALM + "030", "禁用登录页不能被绑定"),
    /**
     * 认证策略不存在
     */
    AUTH_POLICY_DATA_ERROR(REALM + "031", "认证策略不存在"),
    /**
     * 认证策略编码已存在
     */
    AUTH_POLICY_CODE_EXISTS(REALM + "032", "认证策略编码已存在"),
    /**
     * 认证策略参数异常
     */
    AUTH_POLICY_PARAM_ERROR(REALM + "033", "认证策略参数异常"),
    /**
     * 认证策略已被引用
     */
    AUTH_POLICY_REFERENCED(REALM + "034", "认证策略已被身份域或客户端引用，请先解除绑定"),
    /**
     * 系统内置认证策略不能删除
     */
    AUTH_POLICY_SYSTEM_BUILTIN(REALM + "035", "系统内置认证策略不能删除"),
    /**
     * 禁用认证策略不能被绑定
     */
    AUTH_POLICY_DISABLED(REALM + "036", "禁用认证策略不能被绑定"),
    /**
     * 展示忘记密码入口时未配置跳转地址
     */
    LOGIN_PAGE_FORGOT_PASSWORD_URL_REQUIRED(REALM + "037", "显示忘记密码入口时必须配置跳转地址"),
    /**
     * 展示注册入口时未配置跳转地址
     */
    LOGIN_PAGE_REGISTER_URL_REQUIRED(REALM + "038", "显示注册入口时必须配置跳转地址"),
    /**
     * 认证方式不存在
     */
    AUTH_METHOD_DATA_ERROR(REALM + "039", "认证方式不存在"),
    /**
     * 认证方式编码已存在
     */
    AUTH_METHOD_CODE_EXISTS(REALM + "040", "认证方式编码已存在"),
    /**
     * 认证方式参数异常
     */
    AUTH_METHOD_PARAM_ERROR(REALM + "041", "认证方式参数异常"),
    /**
     * 认证方式已被认证策略引用
     */
    AUTH_METHOD_REFERENCED(REALM + "042", "认证方式已被认证策略引用，请先解除绑定"),
    /**
     * 系统内置认证方式不能删除
     */
    AUTH_METHOD_SYSTEM_BUILTIN(REALM + "043", "系统内置认证方式不能删除"),
    /**
     * 禁用认证方式不能用于新配置
     */
    AUTH_METHOD_DISABLED(REALM + "044", "认证方式不存在或未启用"),
    /**
     * 认证方式编码保存后不可修改
     */
    AUTH_METHOD_CODE_IMMUTABLE(REALM + "045", "认证方式编码保存后不可修改"),
    /**
     * 系统内置认证方式模板字段结构不可修改
     */
    AUTH_METHOD_TEMPLATE_LOCKED(REALM + "046", "系统内置认证方式模板字段结构不可修改"),
    /**
     * 扩展认证方式必须配置自定义字段
     */
    AUTH_METHOD_FORM_SCHEMA_REQUIRED(REALM + "047", "扩展认证方式必须配置自定义字段");

    private final String code;

    private final String message;

    RealmErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
