package com.authsphere.server.role.api;

import com.authsphere.server.role.dto.AccountRoleAssignRequest;
import com.authsphere.server.role.dto.CurrentMenuResponse;
import com.authsphere.server.role.dto.CurrentPermissionResponse;
import com.authsphere.server.role.dto.RolePageRequest;
import com.authsphere.server.role.dto.RoleRequest;
import com.authsphere.server.role.dto.RoleResourceAssignRequest;
import com.authsphere.server.role.dto.RoleResourceResponse;
import com.authsphere.server.role.dto.RoleUpdateRequest;
import com.authsphere.server.role.model.AccountRole;
import com.authsphere.server.role.model.Role;
import com.authsphere.server.role.service.RoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IAM 角色与授权接口。
 *
 * <p>该控制器对应 docs/role_auth_doc.md 中的独立 IAM 角色模型，
 * 与应用模块中的 AppClientInstance 角色接口并行存在。</p>
 */
@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表。
     *
     * <p>支持按身份域、主体、客户端、状态和关键字过滤。</p>
     */
    @GetMapping("/api/iam/roles")
    public Page<Role> page(@Validated RolePageRequest request) {
        return roleService.page(request);
    }

    /**
     * 新增角色。
     *
     * <p>角色编码在同一身份域、主体和客户端范围内唯一。</p>
     */
    @PostMapping("/api/iam/roles")
    public Boolean create(@Validated @RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    /**
     * 查询角色详情。
     */
    @GetMapping("/api/iam/roles/{roleId}")
    public Role detail(@PathVariable Long roleId) {
        return roleService.detail(roleId);
    }

    /**
     * 编辑角色基础信息。
     *
     * <p>不允许修改身份域、主体、客户端、角色编码和角色类型等权限边界字段。</p>
     */
    @PutMapping("/api/iam/roles/{roleId}")
    public Boolean edit(@PathVariable Long roleId, @Validated @RequestBody RoleUpdateRequest request) {
        return roleService.edit(roleId, request);
    }

    /**
     * 启用角色。
     */
    @PutMapping("/api/iam/roles/{roleId}/enable")
    public Boolean enable(@PathVariable Long roleId) {
        return roleService.enable(roleId);
    }

    /**
     * 禁用角色。
     *
     * <p>禁用后保留角色资源和账号角色绑定，但不参与最终权限计算。</p>
     */
    @PutMapping("/api/iam/roles/{roleId}/disable")
    public Boolean disable(@PathVariable Long roleId) {
        return roleService.disable(roleId);
    }

    /**
     * 删除角色。
     *
     * <p>系统角色、启用角色、仍绑定账号的角色不能删除。</p>
     */
    @DeleteMapping("/api/iam/roles/{roleId}")
    public Boolean delete(@PathVariable Long roleId) {
        return roleService.delete(roleId);
    }

    /**
     * 查询角色已授权资源。
     */
    @GetMapping("/api/iam/roles/{roleId}/resources")
    public RoleResourceResponse listResources(@PathVariable Long roleId) {
        return roleService.listResources(roleId);
    }

    /**
     * 覆盖保存角色资源授权。
     *
     * <p>资源必须属于角色所在客户端，避免跨客户端授权。</p>
     */
    @PutMapping("/api/iam/roles/{roleId}/resources")
    public Boolean assignResources(@PathVariable Long roleId,
                                   @Validated @RequestBody RoleResourceAssignRequest request) {
        return roleService.assignResources(roleId, request);
    }

    /**
     * 查询账号角色绑定。
     *
     * <p>可按主体和客户端进一步过滤。</p>
     */
    @GetMapping("/api/iam/accounts/{accountId}/roles")
    public List<AccountRole> listAccountRoles(@PathVariable Long accountId,
                                              @RequestParam(required = false) Long subjectId,
                                              @RequestParam(required = false) Long clientId) {
        return roleService.listAccountRoles(accountId, subjectId, clientId);
    }

    /**
     * 覆盖保存账号角色绑定。
     *
     * <p>只覆盖同一账号、同一主体、同一客户端范围内的旧绑定。</p>
     */
    @PostMapping("/api/iam/accounts/{accountId}/roles")
    public Boolean assignAccountRoles(@PathVariable Long accountId,
                                      @Validated @RequestBody AccountRoleAssignRequest request) {
        return roleService.assignAccountRoles(accountId, request);
    }

    /**
     * 获取当前账号菜单树。
     *
     * <p>第一版尚未接入 token 上下文，因此 accountId 暂由请求参数传入。</p>
     */
    @GetMapping("/api/iam/current/menus")
    public List<CurrentMenuResponse> listCurrentMenus(@RequestParam Long accountId,
                                                      @RequestParam(required = false) Long subjectId,
                                                      @RequestParam Long clientId) {
        return roleService.listCurrentMenus(accountId, subjectId, clientId);
    }

    /**
     * 获取当前账号权限编码集合。
     *
     * <p>第一版尚未接入 token 上下文，因此 accountId 暂由请求参数传入。</p>
     */
    @GetMapping("/api/iam/current/permissions")
    public CurrentPermissionResponse listCurrentPermissions(@RequestParam Long accountId,
                                                            @RequestParam(required = false) Long subjectId,
                                                            @RequestParam Long clientId) {
        return roleService.listCurrentPermissions(accountId, subjectId, clientId);
    }
}
