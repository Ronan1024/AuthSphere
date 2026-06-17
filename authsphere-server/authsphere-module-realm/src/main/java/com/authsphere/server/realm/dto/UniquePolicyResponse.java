package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号唯一性规则模板响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniquePolicyResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String code;

    private String name;

    private Boolean usernameUnique;

    private Boolean emailUnique;

    private Boolean mobileUnique;

    private Boolean caseSensitive;

    private Boolean allowEmailReuse;

    private Boolean allowMobileReuse;

    private Integer status;

    private String description;
}
