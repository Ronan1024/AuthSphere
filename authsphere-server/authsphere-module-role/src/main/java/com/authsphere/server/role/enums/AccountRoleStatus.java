package com.authsphere.server.role.enums;

import lombok.Getter;

/**
 * 账号角色绑定状态。
 */
@Getter
public enum AccountRoleStatus {
    /**
     * 启用绑定，参与权限计算。
     */
    ENABLED("enabled"),
    /**
     * 禁用绑定，不参与权限计算。
     */
    DISABLED("disabled");

    /**
     * 数据库存储编码。
     */
    private final String code;

    AccountRoleStatus(String code) {
        this.code = code;
    }
}
