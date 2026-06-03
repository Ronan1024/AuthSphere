package com.authsphere.server.app.dto;

import com.authsphere.server.app.model.App;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientInstance;
import com.authsphere.server.app.model.AppInstance;
import lombok.Data;

/**
 * 登录前置 client_id 解析结果。
 */
@Data
public class AppClientResolveResponse {

    /**
     * 命中的应用客户端。
     */
    private AppClient client;

    /**
     * 命中的应用端实例，登录和角色授权都应绑定到该对象。
     */
    private AppClientInstance clientInstance;

    /**
     * 客户端所属应用实例。
     */
    private AppInstance instance;

    /**
     * 应用实例对应的应用定义。
     */
    private App app;

    /**
     * 客户端默认身份域 ID，登录流程后续据此查找账号。
     */
    private Long realmId;
}
