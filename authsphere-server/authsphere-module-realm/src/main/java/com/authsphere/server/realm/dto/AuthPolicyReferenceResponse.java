package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 认证策略引用对象响应。
 */
@Data
public class AuthPolicyReferenceResponse {

    /**
     * 引用对象主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 引用对象编码。
     */
    private String code;

    /**
     * 引用对象名称。
     */
    private String name;

    /**
     * 引用对象类型，用于区分身份域和客户端。
     */
    private String referenceType;

    /**
     * 引用对象状态，取值遵循统一状态枚举。
     */
    private Integer status;
}
