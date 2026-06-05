package com.authsphere.server.app.dto;

import lombok.Data;

/**
 * 权限资源分页查询请求。
 */
@Data
public class AppPermissionPageRequest {

    /**
     * 当前页码。
     */
    private Integer page = 1;

    /**
     * 每页记录数。
     */
    private Integer size = 10;

    /**
     * 权限类型，可为空：1.BUTTON、2.API、3.DATA。
     */
    private Integer permissionType;
}
