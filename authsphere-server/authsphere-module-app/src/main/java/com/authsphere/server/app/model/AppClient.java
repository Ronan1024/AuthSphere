package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端定义表。
 * @TableName app_client
 */
@TableName(value ="app_client")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClient extends BaseDataBaseModel {
    /**
     * 应用端 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用端编码，同一应用内唯一。
     */
    private String clientCode;

    /**
     * 应用端名称。
     */
    private String clientName;

    /**
     * 应用端类型：1.ADMIN_WEB 2.MERCHANT_WEB 3.MINI_PROGRAM 4.H5 5.OPEN_API 6.SERVICE。
     */
    private Integer clientType;

    /**
     * 客户端密钥，存储时必须保护，接口响应不返回明文或密文。
     */
    @JsonIgnore
    private String clientSecret;

    /**
     * 默认身份域 ID。
     */
    @TableField("default_realm_id")
    private Long realmId;

    /**
     * 默认访问入口。
     */
    private String defaultEntryUrl;

    /**
     * 登录接入方式：IAM_HOSTED/EXTERNAL_REDIRECT/API_ONLY/SERVICE。
     */
    private String loginMode;

    /**
     * 客户自有登录页地址。
     */
    private String externalLoginUrl;

    /**
     * 客户端登录回调地址。
     */
    private String loginCallbackUrl;

    /**
     * 覆盖身份域默认登录页的登录页 ID。
     */
    private Long loginPageId;

    /**
     * 覆盖身份域默认认证策略的认证策略ID。
     */
    private Long authPolicyId;

    /**
     * 客户端绑定的 OSS 外部配置 ID。
     */
    private Long ossConfigId;

    /**
     * ENABLED/DISABLED。
     */
    private Integer status;

    /**
     * 应用端说明。
     */
    private String description;
}
