package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.RoleRequest;
import com.authsphere.server.app.dto.RoleResourceAssignRequest;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.*;
import com.authsphere.server.app.model.*;
import com.authsphere.server.app.service.RoleService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 应用端实例角色服务实现。
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final AppClientInstanceMapper clientInstanceMapper;
    private final AppMenuMapper appMenuMapper;
    private final AppPermissionMapper appPermissionMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Role> listByClientInstance(Long clientInstanceId) {
        ensureEnabledClientInstance(clientInstanceId);
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getClientInstanceId, clientInstanceId)
                .orderByDesc(Role::getCreateTime));
    }

    @Override
    public Role detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long clientInstanceId, RoleRequest request) {
        AppClientInstance clientInstance = ensureEnabledClientInstance(clientInstanceId);
        checkRoleCode(null, clientInstanceId, request.getRoleCode());
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        fillRoleScope(role, clientInstance);
        role.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        roleMapper.insert(role);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, RoleRequest request) {
        Role role = findById(id);
        checkRoleCode(id, role.getClientInstanceId(), request.getRoleCode());
        BeanUtils.copyProperties(request, role, "id", "appCode", "appInstanceId", "clientCode",
                "clientInstanceId", "rootSubjectId", "ownerSubjectId", "createTime", "updateTime");
        role.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        roleMapper.updateById(role);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        Role role = findById(id);
        ensureEnabledClientInstance(role.getClientInstanceId());
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
    public List<AppMenu> listMenus(Long roleId) {
        findById(roleId);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));
        if (CollectionUtils.isEmpty(roleMenus)) {
            return Collections.emptyList();
        }
        return appMenuMapper.selectBatchIds(roleMenus.stream().map(RoleMenu::getMenuId).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignMenus(Long roleId, RoleResourceAssignRequest request) {
        Role role = findById(roleId);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        if (CollectionUtils.isEmpty(request.getResourceIds())) {
            return Boolean.TRUE;
        }
        List<AppMenu> menus = appMenuMapper.selectBatchIds(request.getResourceIds());
        if (menus.size() != request.getResourceIds().size()) {
            throw new BizException(AppErrorCode.ROLE_RESOURCE_SCOPE_ERROR);
        }
        for (AppMenu menu : menus) {
            validateMenuScope(role, menu);
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menu.getId());
            roleMenu.setClientInstanceId(role.getClientInstanceId());
            roleMenu.setCreateTime(LocalDateTime.now());
            roleMenuMapper.insert(roleMenu);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<AppPermission> listPermissions(Long roleId) {
        findById(roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return Collections.emptyList();
        }
        return appPermissionMapper.selectBatchIds(rolePermissions.stream().map(RolePermission::getPermissionId).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignPermissions(Long roleId, RoleResourceAssignRequest request) {
        Role role = findById(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (CollectionUtils.isEmpty(request.getResourceIds())) {
            return Boolean.TRUE;
        }
        List<AppPermission> permissions = appPermissionMapper.selectBatchIds(request.getResourceIds());
        if (permissions.size() != request.getResourceIds().size()) {
            throw new BizException(AppErrorCode.ROLE_RESOURCE_SCOPE_ERROR);
        }
        for (AppPermission permission : permissions) {
            validatePermissionScope(role, permission);
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setClientInstanceId(role.getClientInstanceId());
            rolePermission.setCreateTime(LocalDateTime.now());
            rolePermissionMapper.insert(rolePermission);
        }
        return Boolean.TRUE;
    }

    private void fillRoleScope(Role role, AppClientInstance clientInstance) {
        role.setAppCode(clientInstance.getAppCode());
        role.setAppInstanceId(clientInstance.getAppInstanceId());
        role.setClientCode(clientInstance.getClientCode());
        role.setClientInstanceId(clientInstance.getId());
        role.setRootSubjectId(clientInstance.getRootSubjectId());
        role.setOwnerSubjectId(clientInstance.getOwnerSubjectId());
    }

    private void validateMenuScope(Role role, AppMenu menu) {
        if (!Objects.equals(role.getAppCode(), menu.getAppCode())
                || !Objects.equals(role.getClientCode(), menu.getClientCode())
                || !Objects.equals(menu.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.ROLE_RESOURCE_SCOPE_ERROR);
        }
    }

    private void validatePermissionScope(Role role, AppPermission permission) {
        if (!Objects.equals(role.getAppCode(), permission.getAppCode())
                || !Objects.equals(role.getClientCode(), permission.getClientCode())
                || !Objects.equals(permission.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.ROLE_RESOURCE_SCOPE_ERROR);
        }
    }

    private void checkRoleCode(Long currentId, Long clientInstanceId, String roleCode) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getClientInstanceId, clientInstanceId)
                .eq(Role::getRoleCode, roleCode)
                .ne(currentId != null, Role::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.ROLE_CODE_EXISTS);
        }
    }

    private AppClientInstance ensureEnabledClientInstance(Long clientInstanceId) {
        AppClientInstance clientInstance = clientInstanceMapper.selectById(clientInstanceId);
        if (clientInstance == null || !Objects.equals(clientInstance.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_DATA_ERROR);
        }
        return clientInstance;
    }

    private Role findById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(AppErrorCode.ROLE_DATA_ERROR);
        }
        return role;
    }
}
