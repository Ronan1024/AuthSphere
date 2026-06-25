package com.authsphere.server.subject.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 主体模块内部使用的身份域简要信息。
 */
@Data
public class SubjectRealmOption {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String code;

    private String name;
}
