package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 引用认证方式的认证策略信息。
 */
@Data
public class AuthMethodReferenceResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String code;

    private String name;

    /**
     * 认证方式在策略中的角色，例如主登录、MFA或同时使用。
     */
    private String role;

    private String usage;

    private Integer status;
}
