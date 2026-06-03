package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用定义表
 * @TableName app
 */
@TableName(value ="app")
@Data
@EqualsAndHashCode(callSuper = true)
public class App extends BaseDataBaseModel {
    /**
     * 应用ID
     */
    @TableId
    private Long id;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用类型：IAM/MALL/PAYMENT/WAREHOUSE/LOGISTICS/CUSTOM
     */
    private String appType;

    /**
     * 应用默认入口。
     */
    private String entryUrl;

    /**
     * 应用图标
     */
    private String icon;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 是否内置。
     */
    private Integer builtIn;

    /**
     * 
     */
    private String description;
}
