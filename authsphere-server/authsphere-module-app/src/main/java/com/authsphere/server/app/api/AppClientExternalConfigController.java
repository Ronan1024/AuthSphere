package com.authsphere.server.app.api;

import com.authsphere.server.app.dto.AppClientExternalConfigRequest;
import com.authsphere.server.app.model.AppClientExternalConfig;
import com.authsphere.server.app.service.AppClientExternalConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户端外部平台配置接口。
 */
@RestController
@RequiredArgsConstructor
public class AppClientExternalConfigController {

    private final AppClientExternalConfigService configService;

    /**
     * 查询客户端外部平台配置列表。
     */
    @GetMapping("/api/app-clients/{clientId}/external-configs")
    public List<AppClientExternalConfig> list(@PathVariable Long clientId) {
        return configService.listByClient(clientId);
    }

    /**
     * 新增客户端外部平台配置。
     */
    @PostMapping("/api/app-clients/{clientId}/external-configs")
    public Boolean create(@PathVariable Long clientId,
                          @Validated @RequestBody AppClientExternalConfigRequest request) {
        return configService.create(clientId, request);
    }

    /**
     * 编辑客户端外部平台配置。
     */
    @PutMapping("/api/app-client-external-configs/{configId}")
    public Boolean edit(@PathVariable Long configId,
                        @Validated @RequestBody AppClientExternalConfigRequest request) {
        return configService.edit(configId, request);
    }

    /**
     * 启用客户端外部平台配置。
     */
    @PostMapping("/api/app-client-external-configs/{configId}/enable")
    public Boolean enable(@PathVariable Long configId) {
        return configService.enable(configId);
    }

    /**
     * 禁用客户端外部平台配置。
     */
    @PostMapping("/api/app-client-external-configs/{configId}/disable")
    public Boolean disable(@PathVariable Long configId) {
        return configService.disable(configId);
    }

    /**
     * 校验客户端外部平台配置；一期仅保留入口并校验配置存在。
     */
    @PostMapping("/api/app-client-external-configs/{configId}/verify")
    public Boolean verify(@PathVariable Long configId) {
        return configService.verify(configId);
    }
}
