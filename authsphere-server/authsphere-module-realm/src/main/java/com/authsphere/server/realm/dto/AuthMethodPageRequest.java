package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证方式分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthMethodPageRequest extends PageRequest {

    private String name;

    private String code;

    private String position;

    private Integer status;
}
