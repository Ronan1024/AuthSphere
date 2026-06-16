package com.authsphere.server.realm.dto;

import lombok.Data;

import java.util.Set;

/**
 * 认证方式内置模板响应。
 */
@Data
public class AuthMethodTemplateResponse {

    /**
     * 前端模板标识。
     */
    private String template;

    /**
     * 内置模板默认认证方式编码。
     */
    private String defaultCode;

    /**
     * 内置模板默认认证方式名称。
     */
    private String defaultName;

    /**
     * 模板展示标题。
     */
    private String title;

    /**
     * 模板说明。
     */
    private String description;

    /**
     * 模板允许保存的参数键。
     */
    private Set<String> allowedParamKeys;
}
