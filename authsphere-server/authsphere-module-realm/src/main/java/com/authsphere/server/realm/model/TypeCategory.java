package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 类型项
 *
 * @TableName type_category
 */
@Data
@TableName(value = "type_category")
@EqualsAndHashCode(callSuper = true)
public class TypeCategory extends BaseDataBaseModel {
    /**
     *
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分类类型编号
     */
    private String code;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统内置
     */
    private Boolean systemBuiltin;

    /**
     * 是否允许编辑
     */
    private Boolean editable;

    /**
     * 状态
     */
    private Integer status;
}
