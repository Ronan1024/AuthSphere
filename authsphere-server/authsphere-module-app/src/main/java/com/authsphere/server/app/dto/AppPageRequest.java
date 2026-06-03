package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用定义分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppPageRequest extends PageRequest {

    /**
     * 应用编码，支持模糊查询。
     */
    private String appCode;

    /**
     * 应用名称，支持模糊查询。
     */
    private String appName;

    /**
     * 应用类型。
     */
    private String appType;

    /**
     * 应用状态。
     */
    private Integer status;
}
