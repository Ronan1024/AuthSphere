package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.convert.AppClientPermissionConvert;
import com.authsphere.server.app.domain.AppClientDomain;
import com.authsphere.server.app.domain.AppClientPermissionDomain;
import com.authsphere.server.app.domain.AppMenuDomain;
import com.authsphere.server.app.dto.AppClientPermissionResponse;
import com.authsphere.server.app.dto.AppPermissionPageRequest;
import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.enums.AppPermissionType;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.app.service.AppPermissionService;
import com.authsphere.server.common.enums.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【app_permission(应用权限资源表)】的数据库操作Service实现
 * @createDate 2026-06-03 11:07:48
 */
@Service
@RequiredArgsConstructor
public class AppPermissionServiceImpl extends ServiceImpl<AppPermissionMapper, AppClientPermission> implements AppPermissionService {

    private final AppPermissionMapper appPermissionMapper;
    private final AppClientPermissionDomain appClientPermissionDomain;
    private final AppClientDomain appClientDomain;
    private final AppMenuDomain appMenuDomain;


    @Override
    public List<AppClientPermissionResponse> listByClient(Long appClientId) {
        AppClient client = appClientDomain.findById(appClientId);
        List<AppClientPermission> appClientPermissions = appPermissionMapper.selectList(new LambdaQueryWrapper<AppClientPermission>()
                .eq(AppClientPermission::getAppId, client.getAppId())
                .eq(AppClientPermission::getClientId, client.getId())
                .orderByAsc(AppClientPermission::getId));
        return AppClientPermissionConvert.INSTANCE.toAppClientPermissionResponse(appClientPermissions);
    }


    /**
     * 获取应用客户端权限分页列表
     */
    @Override
    public Page<AppClientPermissionResponse> pageByClient(Long appClientId, AppPermissionPageRequest request) {
        AppClient client = appClientDomain.findById(appClientId);
        Page<AppClientPermissionResponse> page = new Page<>(request.getPage(), request.getSize());
        return appPermissionMapper.page(page, client.getAppId(), client.getId(), request.getPermissionType());
    }

    @Override
    public AppClientPermission detail(Long id) {
        return appClientPermissionDomain.findById(id);
    }

    /**
     * 添加权限信息
     */
    @Override
    public Boolean create(Long appClientId, AppPermissionRequest request) {
        AppClient client = appClientDomain.findById(appClientId);
        appClientPermissionDomain.checkPermissionCode(null, client.getAppId(), client.getId(), request.getPermissionCode());

        if (request.getPermissionType().equals(AppPermissionType.BUTTON.getCode())) {
            appMenuDomain.findById(request.getMenuId());
        }
        AppClientPermission permission = AppClientPermissionConvert.INSTANCE.toAppCLientPermission(request);
        permission.setAppId(client.getAppId());
        permission.setClientId(client.getId());
        appPermissionMapper.insert(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppPermissionRequest request) {
        AppClientPermission permission = appClientPermissionDomain.findById(id);
        appClientPermissionDomain.checkPermissionCode(permission.getId(), permission.getAppId(), permission.getId(), request.getPermissionCode());
        AppClientPermissionConvert.INSTANCE.copyAppClientPermission(permission, request);
        if (request.getPermissionType().equals(AppPermissionType.BUTTON.getCode())) {
            appMenuDomain.findById(request.getMenuId());
        }
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppClientPermission permission = appClientPermissionDomain.findById(id);
        permission.setStatus(StatusEnum.NORMAL.getCode());
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppClientPermission permission = appClientPermissionDomain.findById(id);
        permission.setStatus(StatusEnum.DISABLED.getCode());
        appPermissionMapper.updateById(permission);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        appClientPermissionDomain.findById(id);
        appPermissionMapper.deleteById(id);
        return Boolean.TRUE;
    }


}
