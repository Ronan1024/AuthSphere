package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectTypeResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 主体类型编码。
     */
    private String code;

    /**
     * 主体类型名称。
     */
    private String name;

    /**
     * 主体分类。
     */
    private String category;

    /**
     * 是否允许拥有成员。
     */
    private Boolean canHaveMembers;

    /**
     * 是否允许开通应用。
     */
    private Boolean canOpenApp;

    /**
     * 是否允许分配角色。
     */
    private Boolean canAssignRole;

    /**
     * 是否可以作为数据隔离根主体。
     */
    private Boolean canBeRoot;

    /**
     * 是否系统内置。
     */
    private Boolean builtIn;

    /**
     * 状态。
     */
    private Integer status;

}
