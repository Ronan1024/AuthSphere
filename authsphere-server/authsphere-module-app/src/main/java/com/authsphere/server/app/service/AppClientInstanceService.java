package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppClientInstancePageRequest;
import com.authsphere.server.app.dto.AppClientInstanceRequest;
import com.authsphere.server.app.dto.AppClientResolveResponse;
import com.authsphere.server.app.model.AppClientInstance;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 应用端实例服务。
 */
public interface AppClientInstanceService extends IService<AppClientInstance> {

    /**
     * 分页查询应用端实例。
     */
    Page<AppClientInstance> page(AppClientInstancePageRequest request);

    /**
     * 查询应用实例下已启用或配置的应用端实例。
     */
    List<AppClientInstance> listByAppInstance(Long appInstanceId);

    /**
     * 应用端实例详情。
     */
    AppClientInstance detail(Long id);

    /**
     * 在应用实例下添加应用端实例。
     */
    Boolean create(Long appInstanceId, AppClientInstanceRequest request);

    /**
     * 编辑应用端实例。
     */
    Boolean edit(Long id, AppClientInstanceRequest request);

    /**
     * 启用应用端实例。
     */
    Boolean enable(Long id);

    /**
     * 禁用应用端实例。
     */
    Boolean disable(Long id);

    /**
     * 登录前置解析，通过 client_instance_code 定位应用端实例上下文。
     */
    AppClientResolveResponse resolveByClientInstanceCode(String clientInstanceCode);
}
