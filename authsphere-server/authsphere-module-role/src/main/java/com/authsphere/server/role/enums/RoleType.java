package com.authsphere.server.role.enums;

import lombok.Getter;

/**
 * 角色类型。
 */
@Getter
public enum RoleType {
    /**
     * 系统内置角色，通常不允许删除。
     */
    SYSTEM("system"),
    /**
     * 管理员创建的自定义角色。
     */
    CUSTOM("custom");

    /**
     * 数据库存储编码。
     */
    private final String code;

    RoleType(String code) {
        this.code = code;
    }
}
