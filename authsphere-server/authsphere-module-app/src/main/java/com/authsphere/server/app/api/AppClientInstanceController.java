package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppClientInstancePageRequest;
import com.authsphere.server.app.dto.AppClientInstanceRequest;
import com.authsphere.server.app.dto.AppClientResolveResponse;
import com.authsphere.server.app.model.AppClientInstance;
import com.authsphere.server.app.service.AppClientInstanceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用端实例接口。
 */
@RestController
@RequiredArgsConstructor
public class AppClientInstanceController {

    private final AppClientInstanceService clientInstanceService;

    /**
     * 应用端实例分页列表。
     */
    @PostMapping("/api/app-client-instances/page")
    public Page<AppClientInstance> page(@Validated @RequestBody AppClientInstancePageRequest request) {
        return clientInstanceService.page(request);
    }

    /**
     * 查询应用实例下的应用端实例。
     */
    @GetMapping("/api/app-instances/{instanceId}/client-instances")
    public List<AppClientInstance> listByAppInstance(@PathVariable Long instanceId) {
        return clientInstanceService.listByAppInstance(instanceId);
    }

    /**
     * 在应用实例下新增应用端实例。
     */
    @PostMapping("/api/app-instances/{instanceId}/client-instances")
    public Boolean create(@PathVariable Long instanceId,
                          @Validated @RequestBody AppClientInstanceRequest request) {
        return clientInstanceService.create(instanceId, request);
    }

    /**
     * 应用端实例详情。
     */
    @GetMapping("/api/app-client-instances/{clientInstanceId}")
    public AppClientInstance detail(@PathVariable Long clientInstanceId) {
        return clientInstanceService.detail(clientInstanceId);
    }

    /**
     * 通过应用端实例编码解析登录前置上下文。
     */
    @GetMapping("/api/app-client-instances/resolve")
    public AppClientResolveResponse resolve(@RequestParam String clientInstanceCode) {
        return clientInstanceService.resolveByClientInstanceCode(clientInstanceCode);
    }

    /**
     * 编辑应用端实例。
     */
    @PutMapping("/api/app-client-instances/{clientInstanceId}")
    public Boolean edit(@PathVariable Long clientInstanceId,
                        @Validated @RequestBody AppClientInstanceRequest request) {
        return clientInstanceService.edit(clientInstanceId, request);
    }

    /**
     * 启用应用端实例。
     */
    @PostMapping("/api/app-client-instances/{clientInstanceId}/enable")
    public Boolean enable(@PathVariable Long clientInstanceId) {
        return clientInstanceService.enable(clientInstanceId);
    }

    /**
     * 禁用应用端实例。
     */
    @PostMapping("/api/app-client-instances/{clientInstanceId}/disable")
    public Boolean disable(@PathVariable Long clientInstanceId) {
        return clientInstanceService.disable(clientInstanceId);
    }
}
