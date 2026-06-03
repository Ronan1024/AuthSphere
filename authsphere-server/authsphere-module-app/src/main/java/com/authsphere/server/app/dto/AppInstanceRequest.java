package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用实例提交请求。
 */
@Data
public class AppInstanceRequest {

    /**
     * 开通的应用 ID。
     */
    @NotNull
    private Long appId;

    /**
     * 应用实例归属主体 ID。
     */
    @NotNull
    private Long ownerSubjectId;

    /**
     * 数据隔离根主体 ID；为空时默认使用归属主体的 rootSubjectId 或主体自身 ID。
     */
    private Long rootSubjectId;

    /**
     * 应用实例编码，全局唯一。
     */
    @NotBlank
    private String instanceCode;

    /**
     * 应用实例名称。
     */
    @NotBlank
    private String instanceName;

    /**
     * 开通方式，支持 PLATFORM 或 SELF。
     */
    @NotBlank
    private String openMode;

    /**
     * 实例状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;

    /**
     * 实例扩展配置 JSON。
     */
    private String configJson;
}
