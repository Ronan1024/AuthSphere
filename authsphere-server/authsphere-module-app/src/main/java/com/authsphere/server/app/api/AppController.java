package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppPageRequest;
import com.authsphere.server.app.dto.AppRequest;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.service.AppService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用定义接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apps")
public class AppController {

    private final AppService appService;

    /**
     * 应用定义分页列表。
     */
    @PostMapping("/page")
    public Page<App> page(@Validated @RequestBody AppPageRequest request) {
        return appService.page(request);
    }

    /**
     * 应用定义全量列表。
     */
    @GetMapping
    public List<App> list() {
        return appService.listAll();
    }

    /**
     * 新增应用定义。
     */
    @PostMapping
    public Boolean create(@Validated @RequestBody AppRequest request) {
        return appService.create(request);
    }

    /**
     * 应用定义详情。
     */
    @GetMapping("/{appId}")
    public App detail(@PathVariable Long appId) {
        return appService.detail(appId);
    }

    /**
     * 编辑应用定义。
     */
    @PutMapping("/{appId}")
    public Boolean edit(@PathVariable Long appId, @Validated @RequestBody AppRequest request) {
        return appService.edit(appId, request);
    }

    /**
     * 启用应用定义。
     */
    @PostMapping("/{appId}/enable")
    public Boolean enable(@PathVariable Long appId) {
        return appService.enable(appId);
    }

    /**
     * 禁用应用定义。
     */
    @PostMapping("/{appId}/disable")
    public Boolean disable(@PathVariable Long appId) {
        return appService.disable(appId);
    }
}
