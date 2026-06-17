package com.authsphere.server.subject.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 主体保存请求。
 */
@Data
public class SubjectRequest {

    /**
     * 主体类型 id。
     */
    @NotNull(message = "主体类型不能为空")
    private Long subjectTypeId;

    /**
     * 默认身份域 id。
     */
    @NotNull(message = "身份域不能为空")
    private Long realmId;

    /**
     * 主体编码。
     */
    @NotEmpty(message = "主体编码不能为空")
    private String code;

    /**
     * 主体名称。
     */
    @NotEmpty(message = "主体名称不能为空")
    private String name;

    /**
     * 数据隔离根主体 id。
     */
    private Long rootSubjectId;

    /**
     * 上级主体 id。
     */
    private Long parentSubjectId;

    /**
     * 是否数据隔离根主体：0 否，1 是。
     */
    @NotNull(message = "是否数据隔离根主体不能为空")
    private Integer isRoot;

    /**
     * 是否系统内置主体：0 否，1 是。
     */
    @NotNull(message = "是否系统内置主体不能为空")
    private Integer builtIn;

    /**
     * 描述。
     */
    private String description;
}
