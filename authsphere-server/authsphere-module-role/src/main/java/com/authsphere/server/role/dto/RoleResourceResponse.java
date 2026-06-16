package com.authsphere.server.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 角色已授权资源响应。
 */
@Data
@AllArgsConstructor
public class RoleResourceResponse {

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 当前角色已授权的资源 ID 列表。
     */
    private List<Long> resourceIds;
}
