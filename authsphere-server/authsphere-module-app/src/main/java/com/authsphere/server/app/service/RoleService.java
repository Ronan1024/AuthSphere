package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.RoleRequest;
import com.authsphere.server.app.dto.RoleResourceAssignRequest;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.model.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 应用端实例角色服务。
 */
public interface RoleService extends IService<Role> {

    List<Role> listByClientInstance(Long clientInstanceId);

    Role detail(Long id);

    Boolean create(Long clientInstanceId, RoleRequest request);

    Boolean edit(Long id, RoleRequest request);

    Boolean enable(Long id);

    Boolean disable(Long id);

    List<AppClientMenu> listMenus(Long roleId);

    Boolean assignMenus(Long roleId, RoleResourceAssignRequest request);

    List<AppClientPermission> listPermissions(Long roleId);

    Boolean assignPermissions(Long roleId, RoleResourceAssignRequest request);
}
