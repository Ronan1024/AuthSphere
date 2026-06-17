package com.authsphere.server.role.dto;

import lombok.Data;

import java.util.List;

/**
 * 当前账号菜单响应。
 */
@Data
public class CurrentMenuResponse {

    /**
     * 菜单资源 ID。
     */
    private Long id;

    /**
     * 父级菜单 ID，根菜单为空。
     */
    private Long parentId;

    /**
     * 菜单展示名称。
     */
    private String name;

    /**
     * 前端路由路径。
     */
    private String path;

    /**
     * 前端组件路径。
     */
    private String component;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 菜单对应的权限标识，前端可用于路由或按钮级控制。
     */
    private String permissionCode;

    /**
     * 子菜单列表。
     */
    private List<CurrentMenuResponse> children;
}
