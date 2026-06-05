package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.model.AppMenu;
import com.authsphere.server.app.service.AppMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用菜单资源接口。
 */
@RestController
@RequiredArgsConstructor
public class AppMenuController {

    private final AppMenuService appMenuService;

    /**
     * 查询应用端菜单资源全集。
     */
    @GetMapping("/api/app-clients/{clientId}/menus")
    public List<AppMenuResponse> list(@PathVariable Long clientId) {
        return appMenuService.listByClient(clientId);
    }

    /**
     * 新增应用端菜单资源。
     */
    @PostMapping("/api/app-clients/{clientId}/menus")
    public Boolean create(@PathVariable Long clientId, @Validated @RequestBody AppMenuRequest request) {
        return appMenuService.create(clientId, request);
    }

    /**
     * 查询菜单资源详情。
     */
    @GetMapping("/api/app-client-menus/{menuId}")
    public AppMenu detail(@PathVariable Long menuId) {
        return appMenuService.detail(menuId);
    }

    /**
     * 编辑菜单资源。
     */
    @PutMapping("/api/app-client-menus/{menuId}")
    public Boolean edit(@PathVariable Long menuId, @Validated @RequestBody AppMenuRequest request) {
        return appMenuService.edit(menuId, request);
    }

    /**
     * 启用菜单资源。
     */
    @PostMapping("/api/app-client-menus/{menuId}/enable")
    public Boolean enable(@PathVariable Long menuId) {
        return appMenuService.enable(menuId);
    }

    /**
     * 禁用菜单资源。
     */
    @PostMapping("/api/app-client-menus/{menuId}/disable")
    public Boolean disable(@PathVariable Long menuId) {
        return appMenuService.disable(menuId);
    }
}
