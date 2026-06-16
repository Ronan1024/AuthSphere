package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

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

    /**
     * 已脱敏的认证方式参数，用于列表配置摘要和扩展字段数量展示。
     */
    private Map<String, Object> params;

    @JsonIgnore
    private String positionsText;

    @JsonIgnore
    private String paramsJson;
}
