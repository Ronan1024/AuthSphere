package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型关系规则响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectTypeRelationResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentTypeId;

    private String parentTypeCode;

    private String parentTypeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long childTypeId;

    private String childTypeCode;

    private String childTypeName;

    /**
     * 关系类型：1.MANAGE 2.OWN 3.BELONG 4.SERVICE 5.BIND。
     */
    private Integer relationType;

    /**
     * 是否允许创建。
     */
    private Boolean allowCreate;

    /**
     * 是否允许管理。
     */
    private Boolean allowManage;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 描述。
     */
    private String description;
}
