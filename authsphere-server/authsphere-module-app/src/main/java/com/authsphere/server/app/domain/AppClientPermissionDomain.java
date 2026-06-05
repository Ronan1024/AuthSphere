package com.authsphere.server.app.domain;

import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.app.model.AppInstancePermission;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Service
@RequiredArgsConstructor
public class AppClientPermissionDomain {

    private final AppPermissionMapper appPermissionMapper;

    public AppClientPermission findById(Long id) {
        AppClientPermission permission = appPermissionMapper.selectById(id);
        if (ObjectUtils.isEmpty(permission)) {
            throw new BizException(AppErrorCode.APP_PERMISSION_DATA_ERROR);
        }
        return permission;
    }

    public void checkPermissionCode(Long currentId, Long appId, Long clientId, String permissionCode) {

        Long count = appPermissionMapper.selectCount(new LambdaQueryWrapper<AppClientPermission>()
                .eq(AppClientPermission::getClientId, clientId)
                .eq(AppClientPermission::getAppId, appId)
                .eq(AppClientPermission::getPermissionCode, permissionCode)
                .ne(!ObjectUtils.isEmpty(currentId), AppClientPermission::getId, currentId)
        );

        if (count > 0) {
            throw new BizException(AppErrorCode.APP_PERMISSION_CODE_EXISTS);
        }

    }

}
