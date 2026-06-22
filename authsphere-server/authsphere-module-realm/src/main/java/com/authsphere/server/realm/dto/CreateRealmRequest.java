package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
@Data
public class CreateRealmRequest {

    /**
     * 身份域编码， 唯一
     */
    @NotEmpty(message = "身份域编号不能为空")
    private String code;

    /**
     * 身份域名称
     */
    @NotEmpty(message = "身份域名城不能为空")
    private String name;

    /**
     * 身份域类型id
     */
    @JsonAlias("typeCategoryId")
    private Long realmTypeId;

    /**
     * 默认认证策略ID。
     */
    private Long authPolicyId;

    /**
     * 是否允许注册
     */
    @NotNull(message = "是否允许注册不能为空")
    private Boolean registerEnabled;

    /**
     * 是否开启SSO
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
     * 单点退出策略：enabled 统一退出同域客户端，current_only 仅退出当前客户端。
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
     * 描述
     */
    private String description;
}
