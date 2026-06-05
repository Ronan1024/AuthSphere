package com.authsphere.server.common.enums;

import lombok.Getter;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Getter
public enum PermissionType {
    /**
     * 按钮
     */
    BUTTON(1, "按钮"),
    /**
     * 接口
     */
    API(2, "接口"),
    /**
     * 数据
     */
    DATA(3, "数据"),
    ;

    private final Integer type;

    private final String name;

    PermissionType(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
