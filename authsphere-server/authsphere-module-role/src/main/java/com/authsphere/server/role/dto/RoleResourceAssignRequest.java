package com.authsphere.server.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色资源授权请求。
 */
@Data
public class RoleResourceAssignRequest {

    /**
     * 资源 ID 列表。资源可以是当前客户端下的菜单资源或权限资源。
     */
    @NotNull
    private List<Long> resourceIds;

    /**
     * 操作人账号 ID，用于审计。
     */
    private Long createdBy;
}
