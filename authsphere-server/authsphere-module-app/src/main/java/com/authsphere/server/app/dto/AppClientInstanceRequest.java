package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用端实例提交请求。
 */
@Data
public class AppClientInstanceRequest {

    /**
     * 应用端定义 ID。
     */
    @NotNull
    private Long appClientId;

    /**
     * 应用端实例编码，全局唯一。
     */
    @NotBlank
    private String clientInstanceCode;

    /**
     * 应用端实例名称。
     */
    @NotBlank
    private String clientInstanceName;

    /**
     * 身份域 ID；为空时默认使用应用端定义的 defaultRealmId。
     */
    private Long realmId;

    /**
     * 访问入口；为空时默认使用应用端定义的 defaultEntryUrl。
     */
    private String entryUrl;

    /**
     * 状态。
     */
    @NotNull
    private Integer status;
}
