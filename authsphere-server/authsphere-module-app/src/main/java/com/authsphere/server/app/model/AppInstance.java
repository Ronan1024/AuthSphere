package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用实例表
 * @TableName app_instance
 */
@TableName(value ="app_instance")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInstance extends BaseDataBaseModel {
    /**
     * 应用实例ID
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
     * 应用实例归属主体
     */
    private Long ownerSubjectId;

    /**
     * 数据隔离根主体
     */
    private Long rootSubjectId;

    /**
     * 实例编码
     */
    private String instanceCode;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * PLATFORM/SELF
     */
    private String openMode;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 实例配置JSON
     */
    private String configJson;
}
