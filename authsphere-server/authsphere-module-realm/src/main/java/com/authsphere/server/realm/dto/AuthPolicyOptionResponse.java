package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 启用认证策略选择项。
 */
@Data
public class AuthPolicyOptionResponse {

    /**
     * 认证策略主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 认证策略编码。
     */
    private String code;

    /**
     * 认证策略名称。
     */
    private String name;
}
