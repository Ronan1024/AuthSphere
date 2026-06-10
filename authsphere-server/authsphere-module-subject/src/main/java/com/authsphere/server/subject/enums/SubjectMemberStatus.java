package com.authsphere.server.subject.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 主体成员状态。
 */
@Getter
public enum SubjectMemberStatus {

    ENABLED(1, "启用"),
    DISABLED(2, "禁用"),
    REMOVED(3, "移除");

    private final Integer code;

    private final String desc;

    SubjectMemberStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isRemoved(Integer status) {
        return Objects.equals(REMOVED.code, status);
    }
}
