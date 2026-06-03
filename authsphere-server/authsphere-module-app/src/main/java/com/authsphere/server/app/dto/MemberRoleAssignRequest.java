package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 成员角色授权提交请求。
 */
@Data
public class MemberRoleAssignRequest {

    @NotNull
    private List<Long> roleIds;

    private Long grantedByAccountId;
}
