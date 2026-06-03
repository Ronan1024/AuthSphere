package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    @TableId
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
     * 默认身份域 ID。
     */
    private Long defaultRealmId;

    /**
     * 默认访问入口。
     */
    private String defaultEntryUrl;

    /**
     * ENABLED/DISABLED。
     */
    private Integer status;

    /**
     * 是否内置。
     */
    private Integer builtIn;

    /**
     * 应用端说明。
     */
    private String description;
}
