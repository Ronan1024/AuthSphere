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
     * 独立登录页
     */
    private String loginUrl;

    /**
     * 默认登录页 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long loginPageId;

    /**
     * 默认登录页名称。
     */
    private String loginPageName;

    /**
     * 默认认证策略ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authPolicyId;

    /**
     * 默认认证策略名称。
     */
    private String authPolicyName;


    /**
     * 认证方式列表
     */
    private List<AuthMethodInfoResponse> authMethodList;


    /**
     * 状态
     */
    private Integer status;

}
