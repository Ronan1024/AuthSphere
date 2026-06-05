package com.authsphere.server.app.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Data
public class AppClientPermissionResponse {
    /**
     * 权限ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;



    /**
     * 所属菜单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long menuId;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 1.BUTTON 2.API 3.DATA
     */
    private Integer permissionType;

    /**
     * API 接口路径
     */
    private String apiPath;

    /**
     * HTTP 请求方法
     */
    private String method;

    private String description;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;

}
