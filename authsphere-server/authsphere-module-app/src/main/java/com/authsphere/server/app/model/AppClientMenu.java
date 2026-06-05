package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端菜单资源表。
 *
 * @TableName app_client_menu
 */
@TableName(value = "app_client_menu")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClientMenu extends BaseDataBaseModel {
    /**
     * 菜单ID
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
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 前端路由
     */
    private String routePath;

    /**
     * 组件路径
     */
    private String componentPath;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 源id
     */
    private Long sourceId;
}
