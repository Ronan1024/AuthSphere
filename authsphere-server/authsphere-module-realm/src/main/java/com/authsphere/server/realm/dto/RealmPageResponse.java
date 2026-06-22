package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmPageResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 身份域编码， 唯一
     */
    private String code;

    /**
     * 身份域名称
     */
    private String name;

    /**
     * 身份域类型id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long realmTypeId;

    /**
     * 身份域类型名称
     */
    private String realmTypeName;

    /**
     * 认证方式列表
     */
    private List<AuthMethodInfoResponse> authMethodList;

    /**
     * 是否允许注册。
     */
    private Boolean registerEnabled;

    /**
     * 是否开启 SSO。
     */
    private Boolean ssoEnabled;

    /**
     * SSO 会话有效期，单位小时。
     */
    private Integer ssoSessionTimeout;

    /**
     * SSO 空闲超时，单位分钟。
     */
    private Integer ssoIdleTimeout;

    /**
     * 单点退出策略。
     */
    private String ssoSingleLogout;

    /**
     * 已存在会话处理方式。
     */
    private String existingSessionHandler;

    /**
     * 无 client_id 时的处理方式。
     */
    private String noClientIdHandler;

    /**
     * 默认认证方式ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long defaultAuthMethodId;

    /**
     * 默认认证方式名称
     */
    private String defaultAuthMethodName;

    /**
     * MFA认证方式ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long mfaAuthMethodId;

    /**
     * SSO 客户端数量。
     */
    private Long ssoClientCount;

    /**
     * 账号数量。
     */
    private Long accountCount;

    /**
     * 描述。
     */
    private String description;

    /**
     * 状态
     */
    private Integer status;

}
