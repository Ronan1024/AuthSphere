package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectTypePageRequest extends PageRequest {

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
     * 状态。
     */
    private Integer status;
}
