package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 认证方式选择项响应。
 */
@Data
public class AuthMethodOptionResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String code;

    private String name;

    private List<String> positions;

    @JsonIgnore
    private String positionsText;
}
