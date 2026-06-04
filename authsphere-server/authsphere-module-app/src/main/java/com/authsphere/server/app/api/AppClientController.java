package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppClientPageRequest;
import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.dto.AppClientResponse;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.service.AppClientService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用客户端接口。
 */
@RestController
@RequiredArgsConstructor
public class AppClientController {

    private final AppClientService appClientService;

    /**
     * 查询应用下的应用端列表。
     */
    @GetMapping("/api/apps/{appId}/clients")
    public List<AppClientResponse> listByApp(@PathVariable Long appId) {
        return appClientService.listByApp(appId);
    }

    /**
     * 在应用下新增应用端。
     */
    @PostMapping("/api/apps/{appId}/clients")
    public Boolean create(@PathVariable Long appId, @Validated @RequestBody AppClientRequest request) {
        return appClientService.create(appId, request);
    }

    /**
     * 查询客户端详情。
     */
    @GetMapping("/api/app-clients/{id}")
    public AppClient detail(@PathVariable Long id) {
        return appClientService.detail(id);
    }

    /**
     * 编辑客户端配置。
     */
    @PutMapping("/api/app-clients/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody AppClientRequest request) {
        return appClientService.edit(id, request);
    }

    /**
     * 启用客户端。
     */
    @PostMapping("/api/app-clients/{id}/enable")
    public Boolean enable(@PathVariable Long id) {
        return appClientService.enable(id);
    }

    /**
     * 禁用客户端。
     */
    @PostMapping("/api/app-clients/{id}/disable")
    public Boolean disable(@PathVariable Long id) {
        return appClientService.disable(id);
    }

}
