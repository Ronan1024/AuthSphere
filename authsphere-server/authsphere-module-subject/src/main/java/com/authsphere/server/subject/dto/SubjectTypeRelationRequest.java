package com.authsphere.server.subject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 主体类型关系规则保存请求。
 */
@Data
public class SubjectTypeRelationRequest {

    /**
     * 上级主体类型 id。
     */
    @NotNull(message = "上级主体类型不能为空")
    private Long parentTypeId;

    /**
     * 下级主体类型 id。
     */
    @NotNull(message = "下级主体类型不能为空")
    private Long childTypeId;

    /**
     * 关系类型：1.MANAGE 2.OWN 3.BELONG 4.SERVICE 5.BIND。
     */
    @NotNull(message = "关系类型不能为空")
    private Integer relationType;

    /**
     * 是否允许创建。
     */
    @NotNull(message = "是否允许创建不能为空")
    private Boolean allowCreate;

    /**
     * 是否允许管理。
     */
    @NotNull(message = "是否允许管理不能为空")
    private Boolean allowManage;

    /**
     * 描述。
     */
    private String description;
}
