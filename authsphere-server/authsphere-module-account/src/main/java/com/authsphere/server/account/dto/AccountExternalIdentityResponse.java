package com.authsphere.server.account.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号第三方身份绑定响应。
 */
@Data
public class AccountExternalIdentityResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String providerType;

    private String providerCode;

    private String providerName;

    private String externalUserId;

    private String externalUnionId;

    private String externalAppId;

    private String externalNickname;

    private String externalAvatar;

    private Integer status;

    private LocalDateTime bindAt;

    private LocalDateTime lastLoginTime;
}
