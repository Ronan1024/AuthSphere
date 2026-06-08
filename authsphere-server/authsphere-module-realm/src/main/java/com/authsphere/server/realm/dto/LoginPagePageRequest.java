package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录页分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginPagePageRequest extends PageRequest {
    /**
     * 登录页编号
     */
    private String code;

    /**
     * 登录页名称
     */
    private String name;

    /**
     * 适用身份域类型ID。
     */
    private Long applicableRealmTypeId;

    /**
     * 认证方式编码。
     */
    private String authMethod;

    /**
     * 状态
     */
    private Integer status;
}
