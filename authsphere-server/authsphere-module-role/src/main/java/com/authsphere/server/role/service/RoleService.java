package com.authsphere.server.role.service;

import com.authsphere.server.role.model.Role;
import com.authsphere.server.role.model.AccountRole;
import com.authsphere.server.role.dto.AccountRoleAssignRequest;
import com.authsphere.server.role.dto.CurrentMenuResponse;
import com.authsphere.server.role.dto.CurrentPermissionResponse;
import com.authsphere.server.role.dto.RolePageRequest;
import com.authsphere.server.role.dto.RoleRequest;
import com.authsphere.server.role.dto.RoleResourceAssignRequest;
import com.authsphere.server.role.dto.RoleResourceResponse;
import com.authsphere.server.role.dto.RoleUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * IAM 角色与授权 Service。
 *
 * <p>负责角色生命周期、角色资源授权、账号角色授权，以及当前账号菜单和权限码计算。</p>
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表。
     */
    Page<Role> page(RolePageRequest request);

    /**
     * 查询未逻辑删除的角色详情。
     */
    Role detail(Long id);

    /**
     * 新增角色，并校验角色编码在身份域、主体和客户端范围内唯一。
     */
    Boolean create(RoleRequest request);

    /**
     * 编辑角色基础信息。角色归属边界和角色编码不允许通过该接口修改。
     */
    Boolean edit(Long id, RoleUpdateRequest request);

    /**
     * 启用角色。
     */
    Boolean enable(Long id);

    /**
     * 禁用角色。禁用后账号通过该角色获得的权限不再参与最终计算。
     */
    Boolean disable(Long id);

    /**
     * 删除角色。系统角色、启用角色、仍绑定账号的角色不允许删除。
     */
    Boolean delete(Long id);

    /**
     * 查询角色已授权资源 ID。
     */
    RoleResourceResponse listResources(Long roleId);

    /**
     * 覆盖保存角色资源授权。
     */
    Boolean assignResources(Long roleId, RoleResourceAssignRequest request);

    /**
     * 查询账号在指定主体和客户端下的角色绑定。
     */
    List<AccountRole> listAccountRoles(Long accountId, Long subjectId, Long clientId);

    /**
     * 覆盖保存账号在指定主体和客户端下的角色绑定。
     */
    Boolean assignAccountRoles(Long accountId, AccountRoleAssignRequest request);

    /**
     * 计算当前账号在指定主体和客户端下可见的菜单树。
     */
    List<CurrentMenuResponse> listCurrentMenus(Long accountId, Long subjectId, Long clientId);

    /**
     * 计算当前账号在指定主体和客户端下拥有的权限编码集合。
     */
    CurrentPermissionResponse listCurrentPermissions(Long accountId, Long subjectId, Long clientId);

}
