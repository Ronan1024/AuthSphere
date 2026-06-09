package com.authsphere.server.realm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 认证方式详情响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthMethodDetailResponse extends AuthMethodResponse {

    private Boolean systemBuiltin;

    /**
     * 已脱敏的认证方式参数。
     */
    private Map<String, Object> params;

    private List<AuthMethodReferenceResponse> references;
}
