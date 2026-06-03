package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单或权限授权提交请求。
 */
@Data
public class RoleResourceAssignRequest {

    /**
     * 菜单 ID 或权限 ID 列表。
     */
    @NotNull
    private List<Long> resourceIds;
}
