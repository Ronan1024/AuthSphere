package com.authsphere.server.role.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 角色资源关系表。
 *
 * <p>保存角色与客户端资源之间的授权关系。resource_id 可以指向菜单资源或权限资源，
 * 但必须属于 role.client_id 对应的客户端。</p>
 *
 * @TableName role_resource
 */
@TableName(value ="role_resource")
@Data
public class RoleResource {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 资源 ID。当前实现支持 app_client_menu.id 或 app_client_permission.id。
     */
    private Long resourceId;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
