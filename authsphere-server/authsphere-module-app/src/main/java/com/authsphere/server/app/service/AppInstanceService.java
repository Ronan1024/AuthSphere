package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppInstancePageRequest;
import com.authsphere.server.app.dto.AppInstanceRequest;
import com.authsphere.server.app.model.AppInstance;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_instance(应用实例表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppInstanceService extends IService<AppInstance> {

    /**
     * 分页查询应用实例。
     */
    Page<AppInstance> page(AppInstancePageRequest request);

    /**
     * 查询指定主体已开通的应用实例。
     */
    List<AppInstance> listBySubject(Long subjectId);

    /**
     * 查询应用实例详情。
     */
    AppInstance detail(Long id);

    /**
     * 为请求中的主体开通应用实例，并默认同步应用菜单和权限全集。
     */
    Boolean create(AppInstanceRequest request);

    /**
     * 为路径指定的主体开通应用实例，并默认同步应用菜单和权限全集。
     */
    Boolean createForSubject(Long subjectId, AppInstanceRequest request);

    /**
     * 编辑应用实例基础信息。
     */
    Boolean edit(Long id, AppInstanceRequest request);

    /**
     * 启用应用实例。
     */
    Boolean enable(Long id);

    /**
     * 禁用应用实例；禁用后该实例下客户端不应再登录或签发 Token。
     */
    Boolean disable(Long id);
}
