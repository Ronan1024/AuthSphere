package com.authsphere.server.subject.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体表
 *
 * @TableName subject
 */
@Data
@TableName(value = "subject")
@EqualsAndHashCode(callSuper = true)
public class Subject extends BaseDataBaseModel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主体类型编码
     */
    private Long subjectTypeId;

    /**
     * 默认身份域ID
     */
    private Long realmId;

    /**
     * 主体编码
     */
    private String code;

    /**
     * 主体名称
     */
    private String name;

    /**
     * 状态：ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 数据隔离根主体ID
     */
    private Long rootSubjectId;

    /**
     * 快捷上级主体ID
     */
    private Long parentId;

    /**
     * 是否数据隔离根主体
     */
    private Boolean isRoot;

    /**
     * 是否系统内置主体
     */
    private Boolean builtIn;

    /**
     * 描述
     */
    private String description;
}