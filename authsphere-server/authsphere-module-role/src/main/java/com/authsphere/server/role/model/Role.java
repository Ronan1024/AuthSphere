package com.authsphere.server.role.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * IAM 角色表。
 *
 * <p>角色用于把客户端资源打包成权限集合，并通过账号角色关系授予账号。</p>
 *
 * @TableName role
 */
@TableName(value ="role")
@Data
public class Role {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 身份域ID
     */
    private Long realmId;

    /**
     * 主体ID
     */
    private Long subjectId;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色类型：system/custom/template
     */
    private String roleType;

    /**
     * 数据范围
     */
    private String dataScope;

    /**
     * 状态：1 启用，2 禁用。
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;
}
