package com.authsphere.server.subject.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 主体成员类型。
 */
@Getter
public enum SubjectMemberType {

    OWNER(1, "OWNER"),
    ADMIN(2, "ADMIN"),
    MEMBER(3, "MEMBER"),
    STAFF(4, "STAFF");

    private final Integer code;

    private final String name;

    SubjectMemberType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean valid(Integer code) {
        return Arrays.stream(values()).anyMatch(item -> Objects.equals(item.code, code));
    }
}
