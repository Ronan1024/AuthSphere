package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private Long typeCategoryId;

    /**
     * 是否允许注册
     */
    private Boolean registerEnabled;

    /**
     * 独立登录页
     */
    private String loginUrl;

    /**
     * 是否开启SSO
     */
    private Boolean ssoEnabled;

    /**
     * 密码策略id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long passwordPolicy;

    /**
     * MFA 策略id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long mfaPolicy;

    /**
     * 账号唯一性规则id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uniquePolicy;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
