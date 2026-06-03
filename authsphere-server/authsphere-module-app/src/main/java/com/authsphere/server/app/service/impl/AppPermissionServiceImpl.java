package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.enums.AppPermissionType;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppPermission;
import com.authsphere.server.app.service.AppPermissionService;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppMenu;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author longjiangran
* @description 针对表【app_permission(应用权限资源表)】的数据库操作Service实现
* @createDate 2026-06-03 11:07:48
*/
@Service
@RequiredArgsConstructor
public class AppPermissionServiceImpl extends ServiceImpl<AppPermissionMapper, AppPermission> implements AppPermissionService {

    private final AppPermissionMapper appPermissionMapper;
    private final AppMenuMapper appMenuMapper;
    private final AppClientMapper appClientMapper;

    @Override
    public List<AppPermission> listByClient(Long appClientId) {
        AppClient client = findClient(appClientId);
        return appPermissionMapper.selectList(new LambdaQueryWrapper<AppPermission>()
                .eq(AppPermission::getAppCode, client.getAppCode())
                .eq(AppPermission::getClientCode, client.getClientCode())
                .orderByAsc(AppPermission::getId));
    }

    @Override
    public AppPermission detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long appClientId, AppPermissionRequest request) {
        AppClient client = findClient(appClientId);
        validateRequest(client.getAppCode(), client.getClientCode(), null, request);
        AppPermission permission = new AppPermission();
        BeanUtils.copyProperties(request, permission);
        permission.setAppCode(client.getAppCode());
        permission.setClientCode(client.getClientCode());
        permission.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appPermissionMapper.insert(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppPermissionRequest request) {
        AppPermission permission = findById(id);
        validateRequest(permission.getAppCode(), permission.getClientCode(), id, request);
        BeanUtils.copyProperties(request, permission, "id", "appCode", "clientCode", "createTime", "updateTime");
        permission.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppPermission permission = findById(id);
        permission.setStatus(StatusEnum.NORMAL.getCode());
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppPermission permission = findById(id);
        permission.setStatus(StatusEnum.DISABLED.getCode());
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    private void validateRequest(String appCode, String clientCode, Long currentId, AppPermissionRequest request) {
        if (!AppPermissionType.valid(request.getPermissionType())) {
            throw new BizException(AppErrorCode.APP_PERMISSION_DATA_ERROR);
        }
        Long count = appPermissionMapper.selectCount(new LambdaQueryWrapper<AppPermission>()
                .eq(AppPermission::getAppCode, appCode)
                .eq(AppPermission::getClientCode, clientCode)
                .eq(AppPermission::getPermissionCode, request.getPermissionCode())
                .ne(currentId != null, AppPermission::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_PERMISSION_CODE_EXISTS);
        }
        if (request.getMenuId() != null) {
            AppMenu menu = appMenuMapper.selectById(request.getMenuId());
            if (menu == null
                    || !Objects.equals(menu.getAppCode(), appCode)
                    || !Objects.equals(menu.getClientCode(), clientCode)) {
                throw new BizException(AppErrorCode.APP_PERMISSION_MENU_ERROR);
            }
        }
    }

    private AppClient findClient(Long appClientId) {
        AppClient client = appClientMapper.selectById(appClientId);
        if (client == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        return client;
    }

    private AppPermission findById(Long id) {
        AppPermission permission = appPermissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException(AppErrorCode.APP_PERMISSION_DATA_ERROR);
        }
        return permission;
    }

}

