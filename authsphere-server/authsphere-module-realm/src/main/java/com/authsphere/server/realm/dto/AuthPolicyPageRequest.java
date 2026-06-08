package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证策略分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthPolicyPageRequest extends PageRequest {

    /**
     * 认证策略编码，支持模糊查询。
     */
    private String code;

    /**
     * 认证策略名称，支持模糊查询。
     */
    private String name;

    /**
     * 登录方式编码，用于筛选包含指定登录方式的策略。
     */
    private String authMethod;

    /**
     * 策略状态，取值遵循统一状态枚举。
     */
    private Integer status;
}
