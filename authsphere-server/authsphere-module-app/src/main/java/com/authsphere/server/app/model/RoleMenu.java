package com.authsphere.server.app.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单授权表。
 */
@Data
@TableName(value = "iam_role_menu")
public class RoleMenu {

    @TableId
    private Long id;

    private Long roleId;

    private Long menuId;

    private Long clientInstanceId;

    private LocalDateTime createTime;
}
