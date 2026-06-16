package com.authsphere.server.subject.error;

import com.authsphere.server.common.exception.BaseError;
import lombok.Getter;

import static com.authsphere.server.common.constant.BaseErrorCode.SUBJECT;

/**
 * 主体模块错误码。
 */
@Getter
public enum SubjectErrorCode implements BaseError {
    /**
     * 主体类型数据异常。
     */
    SUBJECT_TYPE_DATA_ERROR(SUBJECT + "001", "主体类型数据异常"),
    /**
     * 主体类型编码已存在。
     */
    SUBJECT_TYPE_CODE_EXISTS(SUBJECT + "002", "主体类型编码已存在"),
    /**
     * 系统内置主体类型不允许删除。
     */
    SUBJECT_TYPE_BUILT_IN_DELETE_DENIED(SUBJECT + "003", "系统内置主体类型不允许删除"),
    /**
     * 主体类型关系规则数据异常。
     */
    SUBJECT_TYPE_RELATION_DATA_ERROR(SUBJECT + "004", "主体类型关系规则数据异常"),
    /**
     * 主体类型关系规则已存在。
     */
    SUBJECT_TYPE_RELATION_EXISTS(SUBJECT + "005", "主体类型关系规则已存在"),
    /**
     * 主体类型关系规则父子类型不能相同。
     */
    SUBJECT_TYPE_RELATION_SELF_DENIED(SUBJECT + "006", "主体类型关系规则父子类型不能相同"),
    /**
     * 主体数据异常。
     */
    SUBJECT_DATA_ERROR(SUBJECT + "007", "主体数据异常"),
    /**
     * 主体编码已存在。
     */
    SUBJECT_CODE_EXISTS(SUBJECT + "008", "主体编码已存在"),
    /**
     * 系统内置主体不允许删除。
     */
    SUBJECT_BUILT_IN_DELETE_DENIED(SUBJECT + "009", "系统内置主体不允许删除"),
    /**
     * 主体父级不能指向自身。
     */
    SUBJECT_PARENT_SELF_DENIED(SUBJECT + "010", "主体父级不能指向自身"),
    /**
     * 主体根节点不能指向自身。
     */
    SUBJECT_ROOT_SELF_DENIED(SUBJECT + "011", "主体根节点不能指向自身"),
    /**
     * 身份域数据异常。
     */
    SUBJECT_REALM_DATA_ERROR(SUBJECT + "012", "身份域数据异常"),
    /**
     * 主体类型不允许维护成员。
     */
    SUBJECT_MEMBER_NOT_ALLOWED(SUBJECT + "013", "当前主体类型不允许维护成员"),
    /**
     * 主体成员数据异常。
     */
    SUBJECT_MEMBER_DATA_ERROR(SUBJECT + "014", "主体成员数据异常"),
    /**
     * 主体成员已存在。
     */
    SUBJECT_MEMBER_EXISTS(SUBJECT + "015", "该账号已是当前主体成员"),
    /**
     * 主体成员类型不合法。
     */
    SUBJECT_MEMBER_TYPE_INVALID(SUBJECT + "016", "主体成员类型不合法"),
    /**
     * 主体成员状态不合法。
     */
    SUBJECT_MEMBER_STATUS_INVALID(SUBJECT + "017", "主体成员状态不合法"),
    /**
     * 账号状态不允许加入或启用成员。
     */
    SUBJECT_MEMBER_ACCOUNT_STATUS_DENIED(SUBJECT + "018", "账号状态不允许加入或启用成员"),
    /**
     * 不能操作最后一个 OWNER 成员。
     */
    SUBJECT_MEMBER_LAST_OWNER_DENIED(SUBJECT + "019", "不能操作当前主体最后一个 OWNER 成员"),
    ;

    private final String code;

    private final String message;

    SubjectErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
