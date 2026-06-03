package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端实例表。
 */
@Data
@TableName(value = "app_client_instance")
@EqualsAndHashCode(callSuper = true)
public class AppClientInstance extends BaseDataBaseModel {

    /**
     * 应用端实例 ID。
     */
    @TableId
    private Long id;

    /**
     * 应用实例 ID。
     */
    private Long appInstanceId;

    /**
     * 应用 ID。
     */
    private Long appId;

    /**
     * 应用编码。
     */
    private String appCode;

    /**
     * 应用端定义 ID。
     */
    private Long appClientId;

    /**
     * 应用端编码。
     */
    private String clientCode;

    /**
     * 归属主体 ID。
     */
    private Long ownerSubjectId;

    /**
     * 数据隔离根主体 ID。
     */
    private Long rootSubjectId;

    /**
     * 应用端实例编码，全局唯一，可作为登录 client_id 使用。
     */
    private String clientInstanceCode;

    /**
     * 应用端实例名称。
     */
    private String clientInstanceName;

    /**
     * 当前端实例使用的身份域。
     */
    private Long realmId;

    /**
     * 当前端实例访问入口。
     */
    private String entryUrl;

    /**
     * 状态。
     */
    private Integer status;
}
