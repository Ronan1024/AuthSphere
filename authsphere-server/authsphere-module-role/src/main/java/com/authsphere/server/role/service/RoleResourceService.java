package com.authsphere.server.role.service;

import com.authsphere.server.role.model.RoleResource;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 角色资源关系基础 Service。
 *
 * <p>复杂授权保存逻辑统一收敛在 {@link RoleService}，这里仅保留 MyBatis-Plus 基础能力。</p>
 */
public interface RoleResourceService extends IService<RoleResource> {

}
