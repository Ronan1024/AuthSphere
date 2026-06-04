package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppClientPageRequest;
import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.dto.AppClientResponse;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.model.AppClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_client(应用客户端表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppClientService extends IService<AppClient> {


    /**
     * 查询指定应用下的应用端列表。
     */
    List<AppClientResponse> listByApp(Long appId);

    /**
     * 同步应用保存时提交的客户端列表。
     * 传入 ID 的客户端执行更新，未传 ID 的客户端执行新增；数据库中存在但本次未传的客户端保持不变。
     */
    void syncClientsForApp(App app, List<AppClientRequest> requests);

    /**
     * 查询应用客户端详情。
     */
    AppClient detail(Long id);

    /**
     * 在指定应用下新增应用端。
     */
    Boolean create(Long appId, AppClientRequest request);

    /**
     * 编辑客户端基础配置。
     */
    Boolean edit(Long id, AppClientRequest request);

    /**
     * 启用客户端。
     */
    Boolean enable(Long id);

    /**
     * 禁用应用端；禁用后其下应用端实例不能登录或访问。
     */
    Boolean disable(Long id);
}
