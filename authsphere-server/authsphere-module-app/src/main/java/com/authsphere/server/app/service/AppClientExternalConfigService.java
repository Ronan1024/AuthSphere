package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppClientExternalConfigRequest;
import com.authsphere.server.app.model.AppClientExternalConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 客户端外部平台配置服务。
 */
public interface AppClientExternalConfigService extends IService<AppClientExternalConfig> {

    /**
     * 查询指定客户端的外部平台配置列表，敏感字段响应时脱敏。
     */
    List<AppClientExternalConfig> listByClient(Long appClientId);

    /**
     * 新增客户端外部平台配置。
     */
    Boolean create(Long appClientId, AppClientExternalConfigRequest request);

    /**
     * 编辑客户端外部平台配置。
     */
    Boolean edit(Long id, AppClientExternalConfigRequest request);

    /**
     * 启用外部平台配置。
     */
    Boolean enable(Long id);

    /**
     * 禁用外部平台配置。
     */
    Boolean disable(Long id);

    /**
     * 预留配置校验入口，一期只校验配置存在。
     */
    Boolean verify(Long id);
}
