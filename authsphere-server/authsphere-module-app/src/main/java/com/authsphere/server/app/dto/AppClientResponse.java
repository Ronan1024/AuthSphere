package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClientResponse extends BaseDataBaseModel {

    /**
     * 应用端 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 应用端编码，同一应用内唯一。
     */
    private String clientCode;

    /**
     * 应用端名称。
     */
    private String clientName;

    /**
     * 应用端类型：1.ADMIN_WEB 2.MERCHANT_WEB 3.MINI_PROGRAM 4.H5 5.OPEN_API 6.SERVICE。
     */
    private Integer clientType;

    /**
     * 默认身份域 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long realmId;

    /**
     * 身份域名称
     */
    private String realmName;

    /**
     * 默认访问入口。
     */
    private String defaultEntryUrl;

    /**
     * 登录接入方式。
     */
    private String loginMode;

    /**
     * 客户自有登录页地址。
     */
    private String externalLoginUrl;

    /**
     * 登录回调地址。
     */
    private String loginCallbackUrl;

    /**
     * 覆盖身份域默认登录页的登录页 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long loginPageId;

    /**
     * 覆盖身份域默认认证策略的认证策略ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authPolicyId;

    /**
     * 客户端绑定的 OSS 外部配置 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ossConfigId;

    /**
     * ENABLED/DISABLED。
     */
    private Integer status;

    /**
     * 应用端说明。
     */
    private String description;
}
