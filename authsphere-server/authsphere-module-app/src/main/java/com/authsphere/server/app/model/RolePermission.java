package com.authsphere.server.app.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限授权表。
 */
@Data
@TableName(value = "iam_role_permission")
public class RolePermission {

    @TableId
    private Long id;

    private Long roleId;

    private Long permissionId;

    private Long clientInstanceId;

    private LocalDateTime createTime;
}
