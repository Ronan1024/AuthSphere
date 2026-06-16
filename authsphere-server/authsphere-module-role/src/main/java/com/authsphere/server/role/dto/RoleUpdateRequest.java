package com.authsphere.server.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 编辑角色请求，只允许修改不影响权限边界的字段。
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色展示名称。
     */
    @NotBlank
    private String roleName;

    /**
     * 数据范围。编辑时不允许修改角色归属边界，只允许调整数据范围。
     */
    private String dataScope;

    /**
     * 角色状态：1 启用，2 禁用。
     */
    private Integer status;

    /**
     * 角色说明。
     */
    private String remark;

    /**
     * 更新人账号 ID，用于审计。
     */
    private Long updatedBy;
}
