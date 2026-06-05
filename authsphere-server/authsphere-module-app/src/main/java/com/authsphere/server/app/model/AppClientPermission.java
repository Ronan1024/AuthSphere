package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端权限资源表。
 * @TableName app_client_permission
 */
@TableName(value ="app_client_permission")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClientPermission extends BaseDataBaseModel {
    /**
     * 权限ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属应用编码
     */
    private Long appId;

    /**
     * 应用端编码
     */
    private Long clientId;

    /**
     * 所属菜单ID
     */
    private Long menuId;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 1.BUTTON 2.API 3.DATA
     */
    private Integer permissionType;

    /**
     * API 接口路径
     */
    private String apiPath;

    /**
     * HTTP 请求方法
     */
    private String method;

    /**
     * 
     */
    private String description;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;
}
