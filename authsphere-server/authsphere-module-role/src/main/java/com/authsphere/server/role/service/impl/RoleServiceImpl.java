package com.authsphere.server.role.service.impl;

import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.common.utils.TreeUtils;
import com.authsphere.server.role.dto.AccountRoleAssignRequest;
import com.authsphere.server.role.dto.CurrentMenuResponse;
import com.authsphere.server.role.dto.CurrentPermissionResponse;
import com.authsphere.server.role.dto.RolePageRequest;
import com.authsphere.server.role.dto.RoleRequest;
import com.authsphere.server.role.dto.RoleResourceAssignRequest;
import com.authsphere.server.role.dto.RoleResourceResponse;
import com.authsphere.server.role.dto.RoleUpdateRequest;
import com.authsphere.server.role.enums.AccountRoleStatus;
import com.authsphere.server.role.enums.RoleType;
import com.authsphere.server.role.error.RoleErrorCode;
import com.authsphere.server.role.mapper.AccountRoleMapper;
import com.authsphere.server.role.mapper.RoleResourceMapper;
import com.authsphere.server.role.model.AccountRole;
import com.authsphere.server.role.model.RoleResource;
import com.authsphere.server.subject.mapper.SubjectMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.role.model.Role;
import com.authsphere.server.role.service.RoleService;
import com.authsphere.server.role.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IAM 角色与授权 Service 实现。
 *
 * <p>实现 docs/role_auth_doc.md 中第一版权限闭环：资源授权给角色，
 * 再将角色分配给账号，最终计算账号菜单和权限码。</p>
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

    /**
     * 默认数据范围：当前主体。
     */
    private static final String DEFAULT_DATA_SCOPE = "current_subject";

    private final RoleMapper roleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final AppClientMapper appClientMapper;
    private final AppMenuMapper appMenuMapper;
    private final AppPermissionMapper appPermissionMapper;
    private final AccountMapper accountMapper;
    private final SubjectMapper subjectMapper;



    @Override
    public Page<Role> page(RolePageRequest request) {
        Page<Role> page = new Page<>(pageNo(request), pageSize(request));
        return roleMapper.page(page, request);
    }

    @Override
    public Role detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(RoleRequest request) {
        // 新增角色前先校验客户端状态和编码唯一性，避免创建出不可授权或边界冲突的角色。
        ensureEnabledClient(request.getClientId());
        ensureSubjectExists(request.getSubjectId());
        checkRoleCode(null, request.getRealmId(), request.getSubjectId(), request.getClientId(), request.getRoleCode());
        Role role = new Role();
        role.setRealmId(request.getRealmId());
        role.setSubjectId(request.getSubjectId());
        role.setClientId(request.getClientId());
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setRoleType(Objects.requireNonNullElse(request.getRoleType(), RoleType.CUSTOM.getCode()));
        role.setDataScope(Objects.requireNonNullElse(request.getDataScope(), DEFAULT_DATA_SCOPE));
        role.setStatus(Objects.requireNonNullElse(request.getStatus(), StatusEnum.NORMAL.getCode()));
        role.setRemark(request.getRemark());
        role.setCreatedBy(request.getCreatedBy());
        role.setDeleted(0);
        roleMapper.insert(role);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, RoleUpdateRequest request) {
        Role role = findById(id);
        // 编辑接口只允许修改展示信息、数据范围、状态和备注，不允许改变身份域、主体、客户端和角色编码。
        role.setRoleName(request.getRoleName());
        role.setDataScope(Objects.requireNonNullElse(request.getDataScope(), role.getDataScope()));
        role.setStatus(Objects.requireNonNullElse(request.getStatus(), role.getStatus()));
        role.setRemark(request.getRemark());
        role.setUpdatedBy(request.getUpdatedBy());
        roleMapper.updateById(role);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        Role role = findById(id);
        ensureEnabledClient(role.getClientId());
        role.setStatus(StatusEnum.NORMAL.getCode());
        roleMapper.updateById(role);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        Role role = findById(id);
        role.setStatus(StatusEnum.DISABLED.getCode());
        roleMapper.updateById(role);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Role role = findById(id);
        // 删除采用保守策略：内置系统角色、启用角色、仍被账号绑定的角色都不能删除。
        if (RoleType.SYSTEM.getCode().equals(role.getRoleType())) {
            throw new BizException(RoleErrorCode.ROLE_SYSTEM_DELETE_DENIED);
        }
        if (Objects.equals(role.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(RoleErrorCode.ROLE_DELETE_DENIED);
        }
        Long accountRoleCount = accountRoleMapper.selectCount(new LambdaQueryWrapper<AccountRole>()
                .eq(AccountRole::getRoleId, id));
        if (accountRoleCount > 0) {
            throw new BizException(RoleErrorCode.ROLE_DELETE_DENIED);
        }
        roleResourceMapper.delete(new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, id));
        role.setDeleted(1);
        roleMapper.updateById(role);
        return Boolean.TRUE;
    }

    @Override
    public RoleResourceResponse listResources(Long roleId) {
        findById(roleId);
        List<Long> resourceIds = roleResourceMapper.selectList(new LambdaQueryWrapper<RoleResource>()
                        .eq(RoleResource::getRoleId, roleId))
                .stream()
                .map(RoleResource::getResourceId)
                .toList();
        return new RoleResourceResponse(roleId, resourceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignResources(Long roleId, RoleResourceAssignRequest request) {
        Role role = findById(roleId);
        if (!Objects.equals(role.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(RoleErrorCode.ROLE_DISABLED_DENIED);
        }
        // 授权保存语义是覆盖式保存：先清空旧授权，再写入本次提交的完整资源集合。
        roleResourceMapper.delete(new LambdaQueryWrapper<RoleResource>().eq(RoleResource::getRoleId, roleId));
        if (CollectionUtils.isEmpty(request.getResourceIds())) {
            return Boolean.TRUE;
        }
        validateResourceScope(role, request.getResourceIds());
        for (Long resourceId : uniqueIds(request.getResourceIds())) {
            RoleResource roleResource = new RoleResource();
            roleResource.setRoleId(roleId);
            roleResource.setResourceId(resourceId);
            roleResource.setClientId(role.getClientId());
            roleResource.setCreatedBy(request.getCreatedBy());
            roleResource.setCreateTime(new Date());
            roleResourceMapper.insert(roleResource);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<AccountRole> listAccountRoles(Long accountId, Long subjectId, Long clientId) {
        return accountRoleMapper.selectList(new LambdaQueryWrapper<AccountRole>()
                .eq(AccountRole::getAccountId, accountId)
                .eq(subjectId != null, AccountRole::getSubjectId, subjectId)
                .eq(clientId != null, AccountRole::getClientId, clientId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignAccountRoles(Long accountId, AccountRoleAssignRequest request) {
        ensureAccountExists(accountId);
        ensureSubjectExists(request.getSubjectId());
        ensureEnabledClient(request.getClientId());
        List<Role> roles = loadRoles(request.getRoleIds());
        if (roles.size() != uniqueIds(request.getRoleIds()).size()) {
            throw new BizException(RoleErrorCode.ROLE_ACCOUNT_SCOPE_ERROR);
        }
        for (Role role : roles) {
            validateAccountRoleScope(role, request.getClientId());
        }
        // 只覆盖同一账号、同一主体、同一客户端下的角色绑定，避免误删其它主体或其它客户端授权。
        accountRoleMapper.delete(new LambdaQueryWrapper<AccountRole>()
                .eq(AccountRole::getAccountId, accountId)
                .eq(request.getSubjectId() != null, AccountRole::getSubjectId, request.getSubjectId())
                .isNull(request.getSubjectId() == null, AccountRole::getSubjectId)
                .eq(AccountRole::getClientId, request.getClientId()));
        for (Long roleId : uniqueIds(request.getRoleIds())) {
            AccountRole accountRole = new AccountRole();
            accountRole.setAccountId(accountId);
            accountRole.setSubjectId(request.getSubjectId());
            accountRole.setClientId(request.getClientId());
            accountRole.setRoleId(roleId);
            accountRole.setStatus(AccountRoleStatus.ENABLED.getCode());
            accountRole.setCreatedBy(request.getCreatedBy());
            accountRole.setCreateTime(new Date());
            accountRoleMapper.insert(accountRole);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CurrentMenuResponse> listCurrentMenus(Long accountId, Long subjectId, Long clientId) {
        Set<Long> resourceIds = currentEnabledResourceIds(accountId, subjectId, clientId);
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 当前菜单只从角色最终资源集合中取启用菜单，并按菜单排序字段组装树。
        List<CurrentMenuResponse> menus = appMenuMapper.selectBatchIds(resourceIds)
                .stream()
                .filter(menu -> Objects.equals(menu.getClientId(), clientId))
                .filter(menu -> Objects.equals(menu.getStatus(), StatusEnum.NORMAL.getCode()))
                .sorted((left, right) -> Integer.compare(
                        Objects.requireNonNullElse(left.getSortNo(), 0),
                        Objects.requireNonNullElse(right.getSortNo(), 0)))
                .map(this::toCurrentMenuResponse)
                .toList();
        return TreeUtils.buildTree(menus, CurrentMenuResponse::getId, CurrentMenuResponse::getParentId, CurrentMenuResponse::setChildren, null);
    }

    @Override
    public CurrentPermissionResponse listCurrentPermissions(Long accountId, Long subjectId, Long clientId) {
        Set<Long> resourceIds = currentEnabledResourceIds(accountId, subjectId, clientId);
        if (resourceIds.isEmpty()) {
            return new CurrentPermissionResponse(Collections.emptyList());
        }
        // 当前权限码只从角色最终资源集合中取启用权限资源，返回去重后的 permission_code。
        List<String> permissions = appPermissionMapper.selectBatchIds(resourceIds)
                .stream()
                .filter(permission -> Objects.equals(permission.getClientId(), clientId))
                .filter(permission -> Objects.equals(permission.getStatus(), StatusEnum.NORMAL.getCode()))
                .map(AppClientPermission::getPermissionCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return new CurrentPermissionResponse(permissions);
    }

    private void checkRoleCode(Long currentId, Long realmId, Long subjectId, Long clientId, String roleCode) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getRealmId, realmId)
                .eq(subjectId != null, Role::getSubjectId, subjectId)
                .isNull(subjectId == null, Role::getSubjectId)
                .eq(Role::getClientId, clientId)
                .eq(Role::getRoleCode, roleCode)
                .eq(Role::getDeleted, 0)
                .ne(currentId != null, Role::getId, currentId));
        if (count > 0) {
            throw new BizException(RoleErrorCode.ROLE_CODE_EXISTS);
        }
    }

    private void validateResourceScope(Role role, List<Long> resourceIds) {
        Set<Long> uniqueResourceIds = uniqueIds(resourceIds);
        Map<Long, AppClientMenu> menus = appMenuMapper.selectBatchIds(uniqueResourceIds)
                .stream()
                .collect(Collectors.toMap(AppClientMenu::getId, Function.identity(), (left, right) -> left));
        Map<Long, AppClientPermission> permissions = appPermissionMapper.selectBatchIds(uniqueResourceIds)
                .stream()
                .collect(Collectors.toMap(AppClientPermission::getId, Function.identity(), (left, right) -> left));
        for (Long resourceId : uniqueResourceIds) {
            AppClientMenu menu = menus.get(resourceId);
            AppClientPermission permission = permissions.get(resourceId);
            // 资源可以是菜单或权限，但无论哪种类型都必须属于角色所在客户端且处于启用状态。
            boolean menuValid = menu != null
                    && Objects.equals(menu.getClientId(), role.getClientId())
                    && Objects.equals(menu.getStatus(), StatusEnum.NORMAL.getCode());
            boolean permissionValid = permission != null
                    && Objects.equals(permission.getClientId(), role.getClientId())
                    && Objects.equals(permission.getStatus(), StatusEnum.NORMAL.getCode());
            if (!menuValid && !permissionValid) {
                throw new BizException(RoleErrorCode.ROLE_RESOURCE_SCOPE_ERROR);
            }
        }
    }

    private Set<Long> currentEnabledResourceIds(Long accountId, Long subjectId, Long clientId) {
        // 先定位账号在当前主体和客户端下的启用角色绑定。
        List<AccountRole> accountRoles = accountRoleMapper.selectList(new LambdaQueryWrapper<AccountRole>()
                .eq(AccountRole::getAccountId, accountId)
                .eq(subjectId != null, AccountRole::getSubjectId, subjectId)
                .isNull(subjectId == null, AccountRole::getSubjectId)
                .eq(AccountRole::getClientId, clientId)
                .eq(AccountRole::getStatus, AccountRoleStatus.ENABLED.getCode()));
        if (CollectionUtils.isEmpty(accountRoles)) {
            return Collections.emptySet();
        }
        Set<Long> roleIds = accountRoles.stream().map(AccountRole::getRoleId).collect(Collectors.toCollection(LinkedHashSet::new));
        // 再过滤角色本身的客户端归属和启用状态，禁用角色不参与最终权限计算。
        Map<Long, Role> roles = loadRoles(new ArrayList<>(roleIds)).stream()
                .filter(role -> Objects.equals(role.getClientId(), clientId))
                .filter(role -> Objects.equals(role.getStatus(), StatusEnum.NORMAL.getCode()))
                .collect(Collectors.toMap(Role::getId, Function.identity(), (left, right) -> left));
        if (roles.isEmpty()) {
            return Collections.emptySet();
        }
        // 最后汇总这些角色在当前客户端下授权的资源 ID，供菜单和权限码接口分别解释。
        return roleResourceMapper.selectList(new LambdaQueryWrapper<RoleResource>()
                        .in(RoleResource::getRoleId, roles.keySet())
                        .eq(RoleResource::getClientId, clientId))
                .stream()
                .map(RoleResource::getResourceId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateAccountRoleScope(Role role, Long clientId) {
        if (!Objects.equals(role.getClientId(), clientId)
                || !Objects.equals(role.getStatus(), StatusEnum.NORMAL.getCode())
                || Objects.equals(role.getDeleted(), 1)) {
            throw new BizException(RoleErrorCode.ROLE_ACCOUNT_SCOPE_ERROR);
        }
    }

    private List<Role> loadRoles(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return roleMapper.selectBatchIds(uniqueIds(roleIds));
    }

    private Role findById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || Objects.equals(role.getDeleted(), 1)) {
            throw new BizException(RoleErrorCode.ROLE_DATA_ERROR);
        }
        return role;
    }

    private AppClient ensureEnabledClient(Long clientId) {
        AppClient client = appClientMapper.selectById(clientId);
        if (client == null || !Objects.equals(client.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(RoleErrorCode.ROLE_CLIENT_ERROR);
        }
        return client;
    }

    private void ensureAccountExists(Long accountId) {
        if (accountMapper != null && accountMapper.selectById(accountId) == null) {
            throw new BizException(RoleErrorCode.ACCOUNT_ROLE_ACCOUNT_ERROR);
        }
    }

    private void ensureSubjectExists(Long subjectId) {
        if (subjectId != null && subjectMapper != null && subjectMapper.selectById(subjectId) == null) {
            throw new BizException(RoleErrorCode.ACCOUNT_ROLE_SUBJECT_ERROR);
        }
    }

    private CurrentMenuResponse toCurrentMenuResponse(AppClientMenu menu) {
        CurrentMenuResponse response = new CurrentMenuResponse();
        response.setId(menu.getId());
        response.setParentId(menu.getParentId());
        response.setName(menu.getMenuName());
        response.setPath(menu.getRoutePath());
        response.setComponent(menu.getComponentPath());
        response.setIcon(menu.getIcon());
        response.setPermissionCode(menu.getMenuCode());
        return response;
    }

    private Set<Long> uniqueIds(List<Long> ids) {
        if (ids == null) {
            return Collections.emptySet();
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private long pageNo(RolePageRequest request) {
        return request.getPage() == null ? 1L : request.getPage();
    }

    private long pageSize(RolePageRequest request) {
        return request.getSize() == null ? 10L : request.getSize();
    }

}




