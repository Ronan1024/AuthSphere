package com.authsphere.server.subject.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型表
 *
 * @TableName subject_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "subject_type")
public class SubjectType extends BaseDataBaseModel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主体类型编码
     */
    private String code;

    /**
     * 主体类型名称
     */
    private String name;

    /**
     * 主体分类
     */
    private String category;

    /**
     * 是否允许拥有成员
     */
    private Boolean canHaveMembers;

    /**
     * 是否允许开通应用
     */
    private Boolean canOpenApp;

    /**
     * 是否允许分配角色
     */
    private Boolean canAssignRole;

    /**
     * 是否可以作为数据隔离根主体
     */
    private Boolean canBeRoot;

    /**
     * 是否系统内置
     */
    private Boolean builtIn;

    /**
     * 状态：ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}