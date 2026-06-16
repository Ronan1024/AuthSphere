package com.authsphere.server.realm.enums;

import com.authsphere.server.realm.dto.AuthMethodTemplateResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 系统预置认证方式模板定义。
 */
public enum AuthMethodTemplate {

    PASSWORD(
            "password",
            "password_login",
            "账号密码",
            "账号密码模板",
            "系统内置。基于 password_hash 进行凭证校验，字段不可自定义。",
            Set.of("passwordPolicy", "maxRetries", "lockDuration")
    ),
    TOTP(
            "totp",
            "totp_login",
            "TOTP 动态口令",
            "TOTP 模板",
            "系统内置。本地算法校验动态口令，配置参数不可自定义。",
            Set.of("totpDigits", "totpPeriod", "totpWindow")
    ),
    CLIENT_CREDENTIALS(
            "client",
            "client_credentials",
            "Client Credentials",
            "Client Credentials",
            "系统内置。用于服务端接口认证，不开放字段结构修改。",
            Set.of("tokenTtl", "signAlg", "allowScope")
    );

    private final String template;
    private final String code;
    private final String defaultName;
    private final String title;
    private final String description;
    private final Set<String> allowedParamKeys;

    AuthMethodTemplate(String template, String code, String defaultName, String title,
                       String description, Set<String> allowedParamKeys) {
        this.template = template;
        this.code = code;
        this.defaultName = defaultName;
        this.title = title;
        this.description = description;
        this.allowedParamKeys = allowedParamKeys;
    }

    public String getTemplate() {
        return template;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getAllowedParamKeys() {
        return allowedParamKeys;
    }

    public static Optional<AuthMethodTemplate> findByCode(String code) {
        return Arrays.stream(values())
                .filter(template -> template.code.equals(code))
                .findFirst();
    }

    public static List<AuthMethodTemplateResponse> listResponses() {
        return Arrays.stream(values())
                .map(AuthMethodTemplate::toResponse)
                .toList();
    }

    private static AuthMethodTemplateResponse toResponse(AuthMethodTemplate template) {
        AuthMethodTemplateResponse response = new AuthMethodTemplateResponse();
        response.setTemplate(template.getTemplate());
        response.setDefaultCode(template.getCode());
        response.setDefaultName(template.getDefaultName());
        response.setTitle(template.getTitle());
        response.setDescription(template.getDescription());
        response.setAllowedParamKeys(template.getAllowedParamKeys());
        return response;
    }
}
