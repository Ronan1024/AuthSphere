package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 登录页选择项响应。
 */
@Data
public class LoginPageOptionResponse {

    /**
     * 登录页ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录页编码。
     */
    private String code;

    /**
     * 登录页名称。
     */
    private String name;

    /**
     * 是否为默认登录页。
     */
    private Boolean defaultPage;
}
