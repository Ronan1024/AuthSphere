package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
public class CreateTypeCategoryRequest {
    /**
     * 分类编号
     */
    @NotEmpty(message = "分类编号不能为空")
    private String code;

    /**
     * 分类名称
     */
    @NotEmpty(message = "分类名称不能为空")
    private String name;


    /**
     * 分类描述
     */
    private String description;

    /**
     * 是否内置分类
     */
    @NotNull(message = "内置分类选项不能为空")
    private Boolean systemBuiltin;


    /**
     * 是否允许编辑
     */
    private Boolean editable;
}
