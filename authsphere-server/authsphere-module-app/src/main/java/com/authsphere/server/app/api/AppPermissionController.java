package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppClientPermissionResponse;
import com.authsphere.server.app.dto.AppPermissionPageRequest;
import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.app.service.AppPermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public List<AppClientPermissionResponse> list(@PathVariable Long clientId) {
        return appPermissionService.listByClient(clientId);
    }

    /**
     * 分页查询应用端权限资源。
     */
    @PostMapping("/api/app-clients/{clientId}/permissions/page")
    public Page<AppClientPermissionResponse> page(@PathVariable Long clientId, @Validated @RequestBody AppPermissionPageRequest request) {
        return appPermissionService.pageByClient(clientId, request);
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
    public AppClientPermission detail(@PathVariable Long permissionId) {
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

    /**
     * 删除权限资源。
     */
    @DeleteMapping("/api/app-client-permissions/{permissionId}")
    public Boolean delete(@PathVariable Long permissionId) {
        return appPermissionService.delete(permissionId);
    }
}
