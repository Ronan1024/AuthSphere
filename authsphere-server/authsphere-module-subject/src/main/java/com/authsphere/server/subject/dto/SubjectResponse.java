package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectTypeId;

    private String subjectTypeCode;

    private String subjectTypeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long realmId;

    private String realmCode;

    private String realmName;

    private String code;

    private String name;

    private Integer status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long rootSubjectId;

    private String rootSubjectCode;

    private String rootSubjectName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentSubjectId;

    private String parentSubjectCode;

    private String parentSubjectName;

    /**
     * 是否数据隔离根主体：0 否，1 是。
     */
    private Boolean isRoot;

    /**
     * 是否系统内置主体：0 否，1 是。
     */
    private Boolean builtIn;

    private String description;
}
