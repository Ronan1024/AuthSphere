package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClientPermission;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppInstancePermission;
import com.authsphere.server.app.service.AppInstancePermissionService;
import com.authsphere.server.app.mapper.AppInstancePermissionMapper;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
* @author longjiangran
* @description 针对表【app_instance_permission(应用实例启用权限表)】的数据库操作Service实现
* @createDate 2026-06-03 11:07:48
*/
@Service
@RequiredArgsConstructor
public class AppInstancePermissionServiceImpl extends ServiceImpl<AppInstancePermissionMapper, AppInstancePermission> implements AppInstancePermissionService {

    private final AppInstancePermissionMapper appInstancePermissionMapper;
    private final AppInstanceMapper appInstanceMapper;
    private final AppPermissionMapper appPermissionMapper;

    @Override
    public List<AppInstancePermission> listByInstance(Long instanceId) {
        ensureInstance(instanceId);
        return appInstancePermissionMapper.selectList(new LambdaQueryWrapper<AppInstancePermission>()
                .eq(AppInstancePermission::getAppInstanceId, instanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncFromApp(Long instanceId) {
        AppInstance instance = ensureInstance(instanceId);
        List<AppClientPermission> permissions = appPermissionMapper.selectList(new LambdaQueryWrapper<AppClientPermission>()
                .eq(AppClientPermission::getAppId, instance.getAppCode()));
        for (AppClientPermission permission : permissions) {
            upsertStatus(instanceId, permission.getId(), StatusEnum.NORMAL.getCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long instanceId, Long permissionId) {
        validateInstancePermission(instanceId, permissionId);
        upsertStatus(instanceId, permissionId, StatusEnum.NORMAL.getCode());
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long instanceId, Long permissionId) {
        validateInstancePermission(instanceId, permissionId);
        upsertStatus(instanceId, permissionId, StatusEnum.DISABLED.getCode());
        return Boolean.TRUE;
    }

    private void upsertStatus(Long instanceId, Long permissionId, Integer status) {
        AppInstancePermission relation = appInstancePermissionMapper.selectOne(new LambdaQueryWrapper<AppInstancePermission>()
                .eq(AppInstancePermission::getAppInstanceId, instanceId)
                .eq(AppInstancePermission::getPermissionId, permissionId));
        if (relation == null) {
            relation = new AppInstancePermission();
            relation.setAppInstanceId(instanceId);
            relation.setPermissionId(permissionId);
            relation.setStatus(status);
            appInstancePermissionMapper.insert(relation);
        } else {
            relation.setStatus(status);
            appInstancePermissionMapper.updateById(relation);
        }
    }

    private void validateInstancePermission(Long instanceId, Long permissionId) {
        AppInstance instance = ensureInstance(instanceId);
        AppClientPermission permission = appPermissionMapper.selectById(permissionId);
        if (permission == null || !Objects.equals(permission.getAppId(), instance.getAppCode())) {
            throw new BizException(AppErrorCode.APP_INSTANCE_RESOURCE_ERROR);
        }
    }

    private AppInstance ensureInstance(Long instanceId) {
        AppInstance instance = appInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DATA_ERROR);
        }
        return instance;
    }

}

