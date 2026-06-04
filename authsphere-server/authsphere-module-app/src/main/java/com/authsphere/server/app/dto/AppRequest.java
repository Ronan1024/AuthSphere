package com.authsphere.server.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 应用定义提交请求。
 */
@Data
public class AppRequest {

    /**
     * 应用编码，全局唯一，创建后不建议修改。
     */
    @NotBlank
    private String appCode;

    /**
     * 应用展示名称。
     */
    @NotBlank
    private String appName;

    /**
     * 应用类型，例如 IAM、MALL、PAYMENT、WAREHOUSE、CUSTOM。
     */
    @NotBlank
    private String appType;

    /**
     * 应用默认入口地址。
     */
    private String entryUrl;


    /**
     * 应用图标标识或图标资源地址。
     */
    private String icon;

    /**
     * 应用状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;

    /**
     * 是否内置应用，1 表示内置，0 表示非内置。
     */
    private Integer builtIn;

    /**
     * 应用说明。
     */
    private String description;

    /**
     * 应用客户端列表；新增或编辑应用时可同时提交多个客户端。
     */
    @Valid
    private List<AppClientRequest> clients;
}
