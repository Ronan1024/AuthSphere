package com.authsphere.server.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增角色请求。
 */
@Data
public class RoleRequest {

    /**
     * 角色所属身份域 ID。
     */
    @NotNull
    private Long realmId;

    /**
     * 角色所属主体 ID。平台角色可为空。
     */
    private Long subjectId;

    /**
     * 角色所属客户端 ID。
     */
    @NotNull
    private Long clientId;

    /**
     * 角色展示名称。
     */
    @NotBlank
    private String roleName;

    /**
     * 角色编码。同一身份域、主体和客户端下必须唯一。
     */
    @NotBlank
    private String roleCode;

    /**
     * 角色类型：system 系统角色，custom 自定义角色；为空时默认 custom。
     */
    private String roleType;

    /**
     * 数据范围，默认 current_subject。
     */
    private String dataScope;

    /**
     * 角色状态：1 启用，2 禁用；为空时默认启用。
     */
    private Integer status;

    /**
     * 角色说明。
     */
    private String remark;

    /**
     * 创建人账号 ID，用于审计。
     */
    private Long createdBy;
}
