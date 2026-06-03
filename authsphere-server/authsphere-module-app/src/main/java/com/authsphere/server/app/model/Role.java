package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端实例角色表。
 */
@Data
@TableName(value = "iam_role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseDataBaseModel {

    @TableId
    private Long id;

    private String appCode;

    private Long appInstanceId;

    private String clientCode;

    private Long clientInstanceId;

    private Long rootSubjectId;

    private Long ownerSubjectId;

    private String roleCode;

    private String roleName;

    /**
     * SYSTEM / CUSTOM。
     */
    private String roleType;

    private Integer status;

    private Integer builtIn;

    private String description;
}
