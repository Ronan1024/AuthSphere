package com.authsphere.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户端外部平台配置提交请求。
 */
@Data
public class AppClientExternalConfigRequest {

    /**
     * 外部平台类型，例如 WECHAT_MINI、ALIPAY_MINI、OIDC、WECHAT_WORK。
     */
    @NotBlank
    private String providerType;

    /**
     * 外部平台配置编码，同一客户端和平台类型下唯一。
     */
    @NotBlank
    private String providerCode;

    /**
     * 外部平台配置名称。
     */
    @NotBlank
    private String providerName;

    /**
     * 第三方应用 ID。
     */
    private String appId;

    /**
     * 第三方应用密钥，保存前会做保护处理，响应时不返回明文。
     */
    private String appSecret;

    /**
     * 第三方公钥。
     */
    private String publicKey;

    /**
     * 应用私钥，保存前会做保护处理，响应时不返回明文。
     */
    private String privateKey;

    /**
     * 第三方平台回调地址。
     */
    private String callbackUrl;

    /**
     * 外部平台扩展配置 JSON。
     */
    private String configJson;

    /**
     * 配置状态，使用 {@code StatusEnum} 的状态值。
     */
    @NotNull
    private Integer status;
}
