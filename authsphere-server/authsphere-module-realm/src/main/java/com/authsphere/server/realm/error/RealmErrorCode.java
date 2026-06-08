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
    TYPE_CATEGORY_NOT_EDITABLE(REALM + "022", "类型分类不可编辑");

    private final String code;

    private final String message;

    RealmErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
