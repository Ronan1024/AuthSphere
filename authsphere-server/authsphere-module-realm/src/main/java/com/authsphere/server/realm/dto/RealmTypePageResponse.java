package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 类型分类响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmTypePageResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 分类编号
     */
    private String code;

    /**
     * 分类的名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 是否系统内置
     */
    private Boolean systemBuiltin;

    /**
     * 是否可修改
     */
    private Boolean editable;

    /**
     * 分类状态
     */
    private Integer status;

    /**
     * 引用身份域数
     */
    private Integer referenceCount;
}
