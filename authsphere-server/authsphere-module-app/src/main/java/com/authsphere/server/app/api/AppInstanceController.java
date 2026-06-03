package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppInstancePageRequest;
import com.authsphere.server.app.dto.AppInstanceRequest;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.app.service.AppInstanceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用实例接口。
 */
@RestController
@RequiredArgsConstructor
public class AppInstanceController {

    private final AppInstanceService appInstanceService;

    /**
     * 应用实例分页列表。
     */
    @PostMapping("/api/app-instances/page")
    public Page<AppInstance> page(@Validated @RequestBody AppInstancePageRequest request) {
        return appInstanceService.page(request);
    }

    /**
     * 为请求中的主体开通应用实例。
     */
    @PostMapping("/api/app-instances")
    public Boolean create(@Validated @RequestBody AppInstanceRequest request) {
        return appInstanceService.create(request);
    }

    /**
     * 应用实例详情。
     */
    @GetMapping("/api/app-instances/{instanceId}")
    public AppInstance detail(@PathVariable Long instanceId) {
        return appInstanceService.detail(instanceId);
    }

    /**
     * 编辑应用实例。
     */
    @PutMapping("/api/app-instances/{instanceId}")
    public Boolean edit(@PathVariable Long instanceId, @Validated @RequestBody AppInstanceRequest request) {
        return appInstanceService.edit(instanceId, request);
    }

    /**
     * 启用应用实例。
     */
    @PostMapping("/api/app-instances/{instanceId}/enable")
    public Boolean enable(@PathVariable Long instanceId) {
        return appInstanceService.enable(instanceId);
    }

    /**
     * 禁用应用实例。
     */
    @PostMapping("/api/app-instances/{instanceId}/disable")
    public Boolean disable(@PathVariable Long instanceId) {
        return appInstanceService.disable(instanceId);
    }

    /**
     * 查询主体已开通的应用实例。
     */
    @GetMapping("/api/subjects/{subjectId}/app-instances")
    public List<AppInstance> listBySubject(@PathVariable Long subjectId) {
        return appInstanceService.listBySubject(subjectId);
    }

    /**
     * 为路径指定主体开通应用实例。
     */
    @PostMapping("/api/subjects/{subjectId}/app-instances")
    public Boolean createForSubject(@PathVariable Long subjectId, @Validated @RequestBody AppInstanceRequest request) {
        return appInstanceService.createForSubject(subjectId, request);
    }
}
