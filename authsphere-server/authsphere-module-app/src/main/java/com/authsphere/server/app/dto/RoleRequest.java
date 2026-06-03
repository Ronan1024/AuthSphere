package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色提交请求。
 */
@Data
public class RoleRequest {

    @NotBlank
    private String roleCode;

    @NotBlank
    private String roleName;

    @NotBlank
    private String roleType;

    @NotNull
    private Integer status;

    private Integer builtIn;

    private String description;
}
