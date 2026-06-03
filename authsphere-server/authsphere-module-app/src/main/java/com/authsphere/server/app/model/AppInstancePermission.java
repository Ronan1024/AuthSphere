package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用实例启用权限表
 * @TableName app_instance_permission
 */
@TableName(value ="app_instance_permission")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInstancePermission extends BaseDataBaseModel {
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
     * 权限ID
     */
    private Long permissionId;

    /**
     * ENABLED/DISABLED
     */
    private Integer status;
}
