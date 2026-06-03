package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.model.AppPermission;
import com.authsphere.server.app.service.AppPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用权限资源接口。
 */
@RestController
@RequiredArgsConstructor
public class AppPermissionController {

    private final AppPermissionService appPermissionService;

    /**
     * 查询应用端权限资源全集。
     */
    @GetMapping("/api/app-clients/{clientId}/permissions")
    public List<AppPermission> list(@PathVariable Long clientId) {
        return appPermissionService.listByClient(clientId);
    }

    /**
     * 新增应用端权限资源。
     */
    @PostMapping("/api/app-clients/{clientId}/permissions")
    public Boolean create(@PathVariable Long clientId, @Validated @RequestBody AppPermissionRequest request) {
        return appPermissionService.create(clientId, request);
    }

    /**
     * 查询权限资源详情。
     */
    @GetMapping("/api/app-client-permissions/{permissionId}")
    public AppPermission detail(@PathVariable Long permissionId) {
        return appPermissionService.detail(permissionId);
    }

    /**
     * 编辑权限资源。
     */
    @PutMapping("/api/app-client-permissions/{permissionId}")
    public Boolean edit(@PathVariable Long permissionId, @Validated @RequestBody AppPermissionRequest request) {
        return appPermissionService.edit(permissionId, request);
    }

    /**
     * 启用权限资源。
     */
    @PostMapping("/api/app-client-permissions/{permissionId}/enable")
    public Boolean enable(@PathVariable Long permissionId) {
        return appPermissionService.enable(permissionId);
    }

    /**
     * 禁用权限资源。
     */
    @PostMapping("/api/app-client-permissions/{permissionId}/disable")
    public Boolean disable(@PathVariable Long permissionId) {
        return appPermissionService.disable(permissionId);
    }
}
