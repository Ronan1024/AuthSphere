package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.RoleRequest;
import com.authsphere.server.app.dto.RoleResourceAssignRequest;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.app.model.Role;
import com.authsphere.server.app.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用端实例角色接口。
 */
@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 查询应用端实例下的角色列表。
     */
    @GetMapping("/api/app-client-instances/{clientInstanceId}/roles")
    public List<Role> listByClientInstance(@PathVariable Long clientInstanceId) {
        return roleService.listByClientInstance(clientInstanceId);
    }

    /**
     * 在应用端实例下新增角色。
     */
    @PostMapping("/api/app-client-instances/{clientInstanceId}/roles")
    public Boolean create(@PathVariable Long clientInstanceId, @Validated @RequestBody RoleRequest request) {
        return roleService.create(clientInstanceId, request);
    }

    /**
     * 角色详情。
     */
    @GetMapping("/api/roles/{roleId}")
    public Role detail(@PathVariable Long roleId) {
        return roleService.detail(roleId);
    }

    /**
     * 编辑角色。
     */
    @PutMapping("/api/roles/{roleId}")
    public Boolean edit(@PathVariable Long roleId, @Validated @RequestBody RoleRequest request) {
        return roleService.edit(roleId, request);
    }

    /**
     * 启用角色。
     */
    @PostMapping("/api/roles/{roleId}/enable")
    public Boolean enable(@PathVariable Long roleId) {
        return roleService.enable(roleId);
    }

    /**
     * 禁用角色。
     */
    @PostMapping("/api/roles/{roleId}/disable")
    public Boolean disable(@PathVariable Long roleId) {
        return roleService.disable(roleId);
    }

    /**
     * 查询角色已授权菜单。
     */
    @GetMapping("/api/roles/{roleId}/menus")
    public List<AppClientMenu> listMenus(@PathVariable Long roleId) {
        return roleService.listMenus(roleId);
    }

    /**
     * 覆盖保存角色菜单授权。
     */
    @PutMapping("/api/roles/{roleId}/menus")
    public Boolean assignMenus(@PathVariable Long roleId,
                               @Validated @RequestBody RoleResourceAssignRequest request) {
        return roleService.assignMenus(roleId, request);
    }

    /**
     * 查询角色已授权权限。
     */
    @GetMapping("/api/roles/{roleId}/permissions")
    public List<AppClientPermission> listPermissions(@PathVariable Long roleId) {
        return roleService.listPermissions(roleId);
    }

    /**
     * 覆盖保存角色权限授权。
     */
    @PutMapping("/api/roles/{roleId}/permissions")
    public Boolean assignPermissions(@PathVariable Long roleId,
                                     @Validated @RequestBody RoleResourceAssignRequest request) {
        return roleService.assignPermissions(roleId, request);
    }
}
