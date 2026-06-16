package com.authsphere.server.app.enums;

import java.util.Arrays;

/**
 * 客户端登录接入方式。
 */
public enum AppClientLoginMode {
    /**
     * 使用认证中心托管登录入口模板。
     */
    IAM_HOSTED,
    /**
     * 跳转到客户自有登录页。
     */
    EXTERNAL_REDIRECT,
    /**
     * 客户系统自有登录体系，仅通过接口注册账号或获取登录信息。
     */
    API_ONLY,
    /**
     * 服务端接口认证，不参与用户登录页流程。
     */
    SERVICE;

    public static AppClientLoginMode of(String value) {
        if (value == null || value.isBlank()) {
            return IAM_HOSTED;
        }
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
