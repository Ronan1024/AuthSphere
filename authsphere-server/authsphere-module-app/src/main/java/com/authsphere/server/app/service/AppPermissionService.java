package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.model.AppPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_permission(应用权限资源表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppPermissionService extends IService<AppPermission> {

    /**
     * 查询指定应用端的权限资源全集。
     */
    List<AppPermission> listByClient(Long appClientId);

    /**
     * 查询权限资源详情。
     */
    AppPermission detail(Long id);

    /**
     * 在指定应用端下新增权限资源。
     */
    Boolean create(Long appClientId, AppPermissionRequest request);

    /**
     * 编辑权限资源。
     */
    Boolean edit(Long id, AppPermissionRequest request);

    /**
     * 启用权限资源。
     */
    Boolean enable(Long id);

    /**
     * 禁用权限资源；实例权限即使启用也不应再参与最终授权。
     */
    Boolean disable(Long id);
}
