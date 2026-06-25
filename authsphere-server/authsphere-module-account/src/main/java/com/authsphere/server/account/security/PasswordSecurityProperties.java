package com.authsphere.server.account.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 密码安全配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "authsphere.password")
public class PasswordSecurityProperties {

    /**
     * 服务端密码 pepper。
     */
    private String pepper;
}
