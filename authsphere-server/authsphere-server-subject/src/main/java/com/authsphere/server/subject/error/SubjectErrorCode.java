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
    ;

    private final String code;

    private final String message;

    SubjectErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
