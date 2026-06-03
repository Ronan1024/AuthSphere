package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用客户端分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClientPageRequest extends PageRequest {

    /**
     * 所属应用 ID。
     */
    private Long appId;

    /**
     * 所属应用编码。
     */
    private String appCode;

    /**
     * 应用端编码，支持模糊查询。
     */
    private String clientCode;

    /**
     * 应用端名称，支持模糊查询。
     */
    private String clientName;

    /**
     * 客户端类型。
     */
    private Integer clientType;

    /**
     * 客户端状态。
     */
    private Integer status;
}
