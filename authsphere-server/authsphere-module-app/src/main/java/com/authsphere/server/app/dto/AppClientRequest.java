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
     * 应用客户端 ID；编辑应用时传入表示更新已有客户端，新增客户端不传。
     */
    private Long id;

    /**
     * 应用端编码，同一应用内唯一。
     */
    @NotBlank(message = "应用端编码不能为空")
    private String clientCode;

    /**
     * 应用端名称。
     */
    @NotBlank(message = "应用端名称不能为空")
    private String clientName;

    /**
     * 应用端类型，1.ADMIN_WEB、2.MERCHANT_WEB、3.MINI_PROGRAM、4.H5、5.OPEN_API、6.SERVICE。
     */
    @NotNull(message = "应用端类型不能为空")
    private Integer clientType;

    /**
     * 客户端密钥。新增时可不传，由服务端自动生成；编辑时为空表示保持原密钥不变。
     */
    private String clientSecret;

    /**
     * 默认身份域 ID。
     */
    private Long realmId;

    /**
     * 默认访问入口地址。
     */
    private String defaultEntryUrl;

    /**
     * 客户端状态，使用 {@code StatusEnum} 的状态值。
     */
    private Integer status;

    /**
     * 应用端说明。
     */
    private String description;
}
