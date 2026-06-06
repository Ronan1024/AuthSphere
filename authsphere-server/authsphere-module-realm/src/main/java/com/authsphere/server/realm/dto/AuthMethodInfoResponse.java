package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
* @program: AuthSphere
* @description:
* @author: L.J.Ran
* @create: 2026/6/6
*/
@Data
public class AuthMethodInfoResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 认证方式名称
     */
    private String name;

    /**
     * 认证方式描述
     */
    private String description;
}
