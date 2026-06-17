package com.authsphere.server.role.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 账号角色关系表。
 *
 * @TableName account_role
 */
@Data
@TableName(value = "account_role")
public class AccountRole {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号 ID。
     */
    private Long accountId;

    /**
     * 主体 ID。为空表示平台级或不绑定具体主体的角色授权。
     */
    private Long subjectId;

    /**
     * 客户端 ID。账号角色授权按客户端隔离。
     */
    private Long clientId;

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * enabled / disabled。
     */
    private String status;

    /**
     * 创建人账号 ID。
     */
    private Long createdBy;

    /**
     * 创建时间。
     */
    private Date createTime;
}
