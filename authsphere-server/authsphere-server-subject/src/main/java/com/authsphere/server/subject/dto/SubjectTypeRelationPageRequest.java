package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型关系规则分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectTypeRelationPageRequest extends PageRequest {

    /**
     * 上级主体类型 id。
     */
    private Long parentTypeId;

    /**
     * 下级主体类型 id。
     */
    private Long childTypeId;

    /**
     * 关系类型：1.MANAGE 2.OWN 3.BELONG 4.SERVICE 5.BIND。
     */
    private Integer relationType;

    /**
     * 状态。
     */
    private Integer status;
}
