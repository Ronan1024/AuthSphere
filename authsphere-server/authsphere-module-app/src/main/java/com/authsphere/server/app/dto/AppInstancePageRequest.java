package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用实例分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInstancePageRequest extends PageRequest {

    /**
     * 应用 ID。
     */
    private Long appId;

    /**
     * 应用编码。
     */
    private String appCode;

    /**
     * 应用实例归属主体 ID。
     */
    private Long ownerSubjectId;

    /**
     * 实例编码，支持模糊查询。
     */
    private String instanceCode;

    /**
     * 实例名称，支持模糊查询。
     */
    private String instanceName;

    /**
     * 实例状态。
     */
    private Integer status;
}
