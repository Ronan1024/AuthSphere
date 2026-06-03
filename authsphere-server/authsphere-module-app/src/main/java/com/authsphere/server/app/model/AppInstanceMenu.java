package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用实例启用菜单表
 * @TableName app_instance_menu
 */
@TableName(value ="app_instance_menu")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInstanceMenu extends BaseDataBaseModel {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 应用实例ID
     */
    private Long appInstanceId;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;
}
