package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用权限资源提交请求。
 */
@Data
public class AppPermissionRequest {

    /**
     * 所属菜单 ID，可为空。
     */
    private Long menuId;

    /**
     * 权限编码，同一应用内唯一。
     */
    @NotBlank
    private String permissionCode;

    /**
     * 权限名称。
     */
    @NotBlank
    private String permissionName;

    /**
     * 权限类型，1.BUTTON、2.API、3.DATA。
     */
    @NotNull
    private Integer permissionType;

    /**
     * 权限说明。
     */
    private String description;

    /**
     * 权限状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;

    /**
     * 是否内置权限，1 表示内置，0 表示非内置。
     */
    private Integer builtIn;
}
