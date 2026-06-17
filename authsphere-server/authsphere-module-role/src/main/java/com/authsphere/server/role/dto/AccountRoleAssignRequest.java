package com.authsphere.server.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 账号角色分配请求。
 */
@Data
public class AccountRoleAssignRequest {

    /**
     * 主体 ID。平台级角色分配可为空；租户、商户等主体内授权时必须传入。
     */
    private Long subjectId;

    /**
     * 客户端 ID。账号角色绑定按客户端隔离，不能跨客户端复用角色。
     */
    @NotNull
    private Long clientId;

    /**
     * 需要分配给账号的角色 ID 列表。保存时会覆盖当前账号在同一主体和客户端下的旧绑定。
     */
    @NotNull
    private List<Long> roleIds;

    /**
     * 操作人账号 ID，用于后续审计追踪。
     */
    private Long createdBy;
}
