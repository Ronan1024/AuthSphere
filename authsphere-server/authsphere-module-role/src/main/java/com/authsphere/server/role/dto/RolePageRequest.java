package com.authsphere.server.role.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageRequest extends PageRequest {

    /**
     * 身份域 ID。
     */
    private Long realmId;

    /**
     * 主体 ID。为空时查询平台级角色或不按主体过滤。
     */
    private Long subjectId;

    /**
     * 客户端 ID。
     */
    private Long clientId;

    /**
     * 角色名称或角色编码关键字。
     */
    private String keyword;

    /**
     * 角色状态：1 启用，2 禁用。
     */
    private Integer status;
}
