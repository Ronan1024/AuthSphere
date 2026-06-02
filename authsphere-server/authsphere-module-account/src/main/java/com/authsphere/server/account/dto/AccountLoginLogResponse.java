package com.authsphere.server.account.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号登录日志响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountLoginLogResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String realmName;

    private String loginEntryCode;

    private String providerType;

    private String loginType;

    private String loginIdentifier;

    private Boolean success;

    private String failReason;

    private String ip;

    private String userAgent;

    private String deviceId;
}
