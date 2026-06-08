package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 类型分类分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmTypePageRequest extends PageRequest {

    /**
     * 分类编号。
     */
    private String code;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 状态。
     */
    private Integer status;
}
