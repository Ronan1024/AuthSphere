package com.authsphere.server.app.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Data
public class AppMenuResponse {

    /**
     * 菜单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属应用编码
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    /**
     * 应用端编码
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long clientId;

    /**
     * 父级菜单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    private Integer visible;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;


    /**
     * 源id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    private List<AppMenuResponse> children;

}
