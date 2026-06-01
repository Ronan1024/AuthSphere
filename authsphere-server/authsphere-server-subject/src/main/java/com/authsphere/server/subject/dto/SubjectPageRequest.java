package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectPageRequest extends PageRequest {

    /**
     * 主体类型 id。
     */
    private Long subjectTypeId;

    /**
     * 身份域 id。
     */
    private Long realmId;

    /**
     * 主体编码。
     */
    private String code;

    /**
     * 主体名称。
     */
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
    private Integer isRoot;

    /**
     * 状态。
     */
    private Integer status;
}
