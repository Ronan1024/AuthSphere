package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 登录页列表响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginPageResponse extends BaseDataBaseModel {

    /**
     * 登录页ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录页编号。
     */
    private String code;

    /**
     * 登录页名称。
     */
    private String name;

    /**
     * 适用身份域类型ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicableRealmTypeId;

    /**
     * 适用身份域类型名称。
     */
    private String applicableRealmTypeName;

    /**
     * 支持的认证方式。
     */
    private List<AuthMethodInfoResponse> authMethod;

    /**
     * 是否默认登录页。
     */
    private Boolean defaultPage;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 引用总数。
     */
    private Integer referenceCount;
}
