package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用客户端提交请求。
 */
@Data
public class AppClientRequest {

    /**
     * 应用端编码，同一应用内唯一。
     */
    @NotBlank
    private String clientCode;

    /**
     * 应用端名称。
     */
    @NotBlank
    private String clientName;

    /**
     * 应用端类型，1.ADMIN_WEB、2.MERCHANT_WEB、3.MINI_PROGRAM、4.H5、5.OPEN_API、6.SERVICE。
     */
    @NotNull
    private Integer clientType;

    /**
     * 默认身份域 ID。
     */
    private Long defaultRealmId;

    /**
     * 默认访问入口地址。
     */
    private String defaultEntryUrl;

    /**
     * 客户端状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;

    /**
     * 是否内置客户端，1 表示内置，0 表示非内置。
     */
    private Integer builtIn;

    /**
     * 应用端说明。
     */
    private String description;
}
