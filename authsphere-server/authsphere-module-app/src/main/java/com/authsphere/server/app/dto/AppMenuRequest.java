package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用菜单资源提交请求。
 */
@Data
public class AppMenuRequest {

    /**
     * 父级菜单 ID，顶级菜单为空。
     */
    private Long parentId;

    /**
     * 菜单编码，同一应用内唯一。
     */
    @NotBlank
    private String menuCode;

    /**
     * 菜单名称。
     */
    @NotBlank
    private String menuName;

    /**
     * 前端路由路径。
     */
    private String routePath;

    /**
     * 前端组件路径。
     */
    private String componentPath;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 菜单排序值，值越小越靠前。
     */
    private Integer sortNo;

    /**
     * 是否在菜单中可见，1 表示可见，0 表示隐藏。
     */
    private Integer visible;

    /**
     * 菜单状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;

    /**
     * 是否内置菜单，1 表示内置，0 表示非内置。
     */
    private Integer builtIn;
}
