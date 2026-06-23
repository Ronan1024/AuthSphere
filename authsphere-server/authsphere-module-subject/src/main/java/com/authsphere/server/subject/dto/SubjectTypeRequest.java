package com.authsphere.server.subject.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 主体类型保存请求。
 */
@Data
public class SubjectTypeRequest {

    /**
     * 主体类型编码。
     */
    @NotEmpty(message = "主体类型编码不能为空")
    private String code;

    /**
     * 主体类型名称。
     */
    @NotEmpty(message = "主体类型名称不能为空")
    private String name;

    /**
     * 主体分类。
     */
    private String category;

    /**
     * 是否允许拥有成员。
     */
    @NotNull(message = "是否允许拥有成员不能为空")
    private Boolean canHaveMembers;

    /**
     * 是否允许开通应用。
     */
    @NotNull(message = "是否允许开通应用不能为空")
    private Boolean canOpenApp;

    /**
     * 是否允许分配角色。
     */
    @NotNull(message = "是否允许分配角色不能为空")
    private Boolean canAssignRole;

    /**
     * 是否可以作为数据隔离根主体。
     */
    @NotNull(message = "是否可以作为数据隔离根主体不能为空")
    private Boolean canBeRoot;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 描述。
     */
    private String description;
}
