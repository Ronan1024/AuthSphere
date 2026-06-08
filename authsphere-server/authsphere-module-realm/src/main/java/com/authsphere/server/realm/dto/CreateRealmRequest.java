package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    private Long RealmTypeId;

    /**
     * 独立登录页
     */
    private String loginUrl;

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
     * 密码策略id。
     */
    private Long passwordPolicy;

    /**
     * MFA 策略id。
     */
    private Long mfaPolicy;

    /**
     * 账号唯一性规则id。
     */
    private Long uniquePolicy;

    /**
     * 描述
     */
    private String description;
}
