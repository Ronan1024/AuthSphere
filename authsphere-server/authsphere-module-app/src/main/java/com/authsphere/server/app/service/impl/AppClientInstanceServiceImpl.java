package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppClientInstancePageRequest;
import com.authsphere.server.app.dto.AppClientInstanceRequest;
import com.authsphere.server.app.dto.AppClientResolveResponse;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientInstanceMapper;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.authsphere.server.app.mapper.AppMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientInstance;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.app.service.AppClientInstanceService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 应用端实例服务实现。
 */
@Service
@RequiredArgsConstructor
public class AppClientInstanceServiceImpl
        extends ServiceImpl<AppClientInstanceMapper, AppClientInstance>
        implements AppClientInstanceService {

    private final AppClientInstanceMapper clientInstanceMapper;
    private final AppInstanceMapper appInstanceMapper;
    private final AppClientMapper appClientMapper;
    private final AppMapper appMapper;
    private final RealmDomain realmDomain;

    @Override
    public Page<AppClientInstance> page(AppClientInstancePageRequest request) {
        Page<AppClientInstance> page = new Page<>(request.getPage(), request.getSize());
        return clientInstanceMapper.page(page, request);
    }

    @Override
    public List<AppClientInstance> listByAppInstance(Long appInstanceId) {
        ensureEnabledAppInstance(appInstanceId);
        return clientInstanceMapper.selectList(new LambdaQueryWrapper<AppClientInstance>()
                .eq(AppClientInstance::getAppInstanceId, appInstanceId)
                .orderByDesc(AppClientInstance::getCreateTime));
    }

    @Override
    public AppClientInstance detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long appInstanceId, AppClientInstanceRequest request) {
        AppInstance appInstance = ensureEnabledAppInstance(appInstanceId);
        AppClient appClient = validateRequest(null, appInstance, request);
        AppClientInstance instance = new AppClientInstance();
        BeanUtils.copyProperties(request, instance);
        fillByAppInstanceAndClient(instance, appInstance, appClient);
        clientInstanceMapper.insert(instance);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppClientInstanceRequest request) {
        AppClientInstance instance = findById(id);
        AppInstance appInstance = ensureEnabledAppInstance(instance.getAppInstanceId());
        request.setAppClientId(instance.getAppClientId());
        AppClient appClient = validateRequest(id, appInstance, request);
        BeanUtils.copyProperties(request, instance, "id", "appInstanceId", "appId", "appCode",
                "appClientId", "clientCode", "ownerSubjectId", "rootSubjectId", "createTime", "updateTime");
        fillDefaultRealmAndEntry(instance, appClient);
        clientInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppClientInstance instance = findById(id);
        ensureEnabledAppInstance(instance.getAppInstanceId());
        ensureEnabledAppClient(instance.getAppClientId());
        validateRealm(instance.getRealmId());
        instance.setStatus(StatusEnum.NORMAL.getCode());
        clientInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppClientInstance instance = findById(id);
        instance.setStatus(StatusEnum.DISABLED.getCode());
        clientInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    @Override
    public AppClientResolveResponse resolveByClientInstanceCode(String clientInstanceCode) {
        // 登录入口必须落到应用端实例，避免同一应用实例下多端资源和角色混用。
        AppClientInstance clientInstance = clientInstanceMapper.selectOne(new LambdaQueryWrapper<AppClientInstance>()
                .eq(AppClientInstance::getClientInstanceCode, clientInstanceCode));
        if (clientInstance == null || !Objects.equals(clientInstance.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_DATA_ERROR);
        }
        AppInstance appInstance = ensureEnabledAppInstance(clientInstance.getAppInstanceId());
        AppClient appClient = ensureEnabledAppClient(clientInstance.getAppClientId());
        App app = ensureEnabledApp(appInstance.getAppId());
        validateRealm(clientInstance.getRealmId());

        AppClientResolveResponse response = new AppClientResolveResponse();
        response.setClient(appClient);
        response.setClientInstance(clientInstance);
        response.setInstance(appInstance);
        response.setApp(app);
        response.setRealmId(clientInstance.getRealmId());
        return response;
    }

    private AppClient validateRequest(Long currentId, AppInstance appInstance, AppClientInstanceRequest request) {
        AppClient appClient = ensureEnabledAppClient(request.getAppClientId());
        if (!Objects.equals(appInstance.getAppId(), appClient.getAppId())) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_APP_MISMATCH);
        }
        Long codeCount = clientInstanceMapper.selectCount(new LambdaQueryWrapper<AppClientInstance>()
                .eq(AppClientInstance::getClientInstanceCode, request.getClientInstanceCode())
                .ne(currentId != null, AppClientInstance::getId, currentId));
        if (codeCount > 0) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_CODE_EXISTS);
        }
        Long clientCount = clientInstanceMapper.selectCount(new LambdaQueryWrapper<AppClientInstance>()
                .eq(AppClientInstance::getAppInstanceId, appInstance.getId())
                .eq(AppClientInstance::getClientCode, appClient.getClientCode())
                .ne(currentId != null, AppClientInstance::getId, currentId));
        if (clientCount > 0) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_EXISTS);
        }
//        Long realmId = request.getRealmId() != null ? request.getRealmId() : appClient.getDefaultRealmId();
//        validateRealm(realmId);
        return appClient;
    }

    private void fillByAppInstanceAndClient(AppClientInstance instance, AppInstance appInstance, AppClient appClient) {
        instance.setAppInstanceId(appInstance.getId());
        instance.setAppId(appInstance.getAppId());
        instance.setAppCode(appInstance.getAppCode());
        instance.setAppClientId(appClient.getId());
        instance.setClientCode(appClient.getClientCode());
        instance.setOwnerSubjectId(appInstance.getOwnerSubjectId());
        instance.setRootSubjectId(appInstance.getRootSubjectId());
        fillDefaultRealmAndEntry(instance, appClient);
    }

    private void fillDefaultRealmAndEntry(AppClientInstance instance, AppClient appClient) {
        if (instance.getRealmId() == null) {
//            instance.setRealmId(appClient.getDefaultRealmId());
        }
        if (instance.getEntryUrl() == null || instance.getEntryUrl().isBlank()) {
            instance.setEntryUrl(appClient.getDefaultEntryUrl());
        }
    }

    private AppInstance ensureEnabledAppInstance(Long appInstanceId) {
        AppInstance appInstance = appInstanceMapper.selectById(appInstanceId);
        if (appInstance == null) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DATA_ERROR);
        }
        if (!Objects.equals(appInstance.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DISABLED_DENIED);
        }
        return appInstance;
    }

    private AppClient ensureEnabledAppClient(Long appClientId) {
        AppClient appClient = appClientMapper.selectById(appClientId);
        if (appClient == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        if (!Objects.equals(appClient.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_CLIENT_DISABLED_DENIED);
        }
        return appClient;
    }

    private App ensureEnabledApp(Long appId) {
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        if (!Objects.equals(app.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_DISABLED_DENIED);
        }
        return app;
    }

    private void validateRealm(Long realmId) {
        if (realmId == null) {
            throw new BizException(AppErrorCode.APP_REALM_DATA_ERROR);
        }
        Realm realm = realmDomain.findById(realmId);
        if (realm == null || !Objects.equals(realm.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_REALM_DATA_ERROR);
        }
    }

    private AppClientInstance findById(Long id) {
        AppClientInstance instance = clientInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_DATA_ERROR);
        }
        return instance;
    }
}
