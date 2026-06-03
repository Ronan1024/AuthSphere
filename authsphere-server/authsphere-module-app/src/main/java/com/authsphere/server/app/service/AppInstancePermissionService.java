package com.authsphere.server.app.service;

import com.authsphere.server.app.model.AppInstancePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_instance_permission(应用实例启用权限表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppInstancePermissionService extends IService<AppInstancePermission> {

    /**
     * 查询应用实例已配置的可用权限。
     */
    List<AppInstancePermission> listByInstance(Long instanceId);

    /**
     * 从应用权限资源全集同步到实例权限配置，已存在配置只保留并刷新为启用状态。
     */
    Boolean syncFromApp(Long instanceId);

    /**
     * 启用实例下指定权限。
     */
    Boolean enable(Long instanceId, Long permissionId);

    /**
     * 禁用实例下指定权限。
     */
    Boolean disable(Long instanceId, Long permissionId);
}
