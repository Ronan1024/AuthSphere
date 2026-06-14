package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 认证方式简要信息，用于登录页和身份域等关联接口。
 */
@Data
public class AuthMethodInfoResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 认证方式名称。
     */
    private String name;

    /**
     * 认证方式描述。
     */
    private String description;
}
