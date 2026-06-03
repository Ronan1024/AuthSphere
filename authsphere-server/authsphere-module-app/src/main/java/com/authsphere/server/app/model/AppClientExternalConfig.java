package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用客户端外部平台配置表。
 */
@Data
@TableName(value = "app_client_external_config")
@EqualsAndHashCode(callSuper = true)
public class AppClientExternalConfig extends BaseDataBaseModel {

    /**
     * 主键 ID。
     */
    @TableId
    private Long id;

    /**
     * 所属应用客户端 ID。
     */
    private Long appClientId;

    /**
     * 外部平台类型。
     */
    private String providerType;

    /**
     * 外部平台配置编码。
     */
    private String providerCode;

    /**
     * 外部平台配置名称。
     */
    private String providerName;

    /**
     * 第三方应用 ID。
     */
    private String appId;

    /**
     * 第三方应用密钥，存储时必须保护，响应时必须脱敏。
     */
    private String appSecret;

    /**
     * 第三方公钥。
     */
    private String publicKey;

    /**
     * 应用私钥，存储时必须保护，响应时必须脱敏。
     */
    private String privateKey;

    /**
     * 外部平台回调地址。
     */
    private String callbackUrl;

    /**
     * 扩展配置 JSON。
     */
    private String configJson;

    /**
     * 配置状态。
     */
    private Integer status;
}
