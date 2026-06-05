package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.service.AppClientMenuService;
import com.authsphere.server.common.enums.StatusEnum;
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

    private final AppClientMenuService appClientMenuService;

    /**
     * 查询应用端菜单资源全集。
     */
    @GetMapping("/api/app-clients/{clientId}/menus")
    public List<AppMenuResponse> list(@PathVariable Long clientId) {
        return appClientMenuService.listByClient(clientId);
    }

    /**
     * 新增应用端菜单资源。
     */
    @PostMapping("/api/app-clients/{clientId}/menus")
    public Boolean create(@PathVariable Long clientId, @Validated @RequestBody AppMenuRequest request) {
        return appClientMenuService.create(clientId, request);
    }

    /**
     * 查询菜单资源详情。
     */
    @GetMapping("/api/app-client-menus/{menuId}")
    public AppClientMenu detail(@PathVariable Long menuId) {
        return appClientMenuService.detail(menuId);
    }

    /**
     * 编辑菜单资源。
     */
    @PutMapping("/api/app-client-menus/{menuId}")
    public Boolean edit(@PathVariable Long menuId, @Validated @RequestBody AppMenuRequest request) {
        return appClientMenuService.edit(menuId, request);
    }

    /**
     * 启用菜单资源。
     */
    @PostMapping("/api/app-client-menus/{menuId}/enable")
    public Boolean enable(@PathVariable Long menuId) {
        return appClientMenuService.changeMenuStatus(menuId, StatusEnum.NORMAL);
    }

    /**
     * 禁用菜单资源。
     */
    @PostMapping("/api/app-client-menus/{menuId}/disable")
    public Boolean disable(@PathVariable Long menuId) {
        return appClientMenuService.changeMenuStatus(menuId, StatusEnum.DISABLED);
    }

    /**
     * 删除菜单资源。
     */
    @DeleteMapping("/api/app-client-menus/{menuId}")
    public Boolean delete(@PathVariable Long menuId) {
        return appClientMenuService.delete(menuId);
    }
}
