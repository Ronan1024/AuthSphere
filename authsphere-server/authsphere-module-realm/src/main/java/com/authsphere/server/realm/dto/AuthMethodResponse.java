package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 认证方式分页列表响应，仅返回列表展示所需字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthMethodResponse extends BaseDataBaseModel {

    /**
     * 认证方式ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 认证方式编码
     */
    private String code;

    /**
     * 认证方式名称
     */
    private String name;

    /**
     *
     */
    private List<String> positions;

    private String applicableRange;

    private Integer status;

    /**
     * 关联引用数量
     */
    private Integer referenceCount;

    /**
     * 排序编号
     */
    private Integer sortNo;

    /**
     * 认证方式描述
     */
    private String description;

    @JsonIgnore
    private String positionsText;
}
