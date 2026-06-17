package com.authsphere.server.subject.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体类型关系规则表
 * @TableName subject_type_relation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value ="subject_type_relation")
public class SubjectTypeRelation extends BaseDataBaseModel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上级主体类型id
     */
    private Long parentTypeId;

    /**
     * 下级主体类型id
     */
    private Long childTypeId;

    /**
     * 关系类型：1.MANAGE 2.OWN 3.BELONG 4.SERVICE 5.BIND
     */
    private Integer relationType;

    /**
     * 是否允许创建
     */
    private Boolean allowCreate;

    /**
     * 是否允许管理
     */
    private Boolean allowManage;

    /**
     * 状态：ENABLED/DISABLED
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}