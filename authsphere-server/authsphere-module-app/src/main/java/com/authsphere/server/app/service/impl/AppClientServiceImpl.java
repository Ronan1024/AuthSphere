package com.authsphere.server.app.service.impl;

import com.authsphere.server.api.RealmApi;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.app.convert.AppClientConvert;
import com.authsphere.server.app.domain.AppDomain;
import com.authsphere.server.app.dto.AppClientPageRequest;
import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.dto.AppClientResponse;
import com.authsphere.server.app.enums.AppClientType;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.service.AppClientService;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author longjiangran
 * @description 针对表【app_client(应用客户端表)】的数据库操作Service实现
 * @createDate 2026-06-03 11:07:48
 */
@Service
@RequiredArgsConstructor
public class AppClientServiceImpl extends ServiceImpl<AppClientMapper, AppClient> implements AppClientService {

    private final AppClientMapper appClientMapper;
    private final AppMapper appMapper;
    private final RealmDomain realmDomain;
    private final AppDomain appDomain;
    private final RealmApi realmApi;


    /**
     * 获取应用客户端列表
     *
     * @param appId
     * @return
     */
    @Override
    public List<AppClientResponse> listByApp(Long appId) {
        appDomain.findById(appId);

        List<AppClient> appClients = appClientMapper.selectList(new LambdaQueryWrapper<AppClient>()
                .eq(AppClient::getAppId, appId));
        if (CollectionUtils.isEmpty(appClients)) {
            return Collections.emptyList();
        }
        List<Long> realmIdList = appClients.stream().map(AppClient::getRealmId).toList();
        List<RealmInfoResponse> realmInfoResponses = realmApi.list(realmIdList);
        Map<Long, RealmInfoResponse> map = realmInfoResponses.stream().collect(Collectors.toMap(RealmInfoResponse::getId, e -> e));
        return appClients.stream().map(e -> {
            RealmInfoResponse infoResponse = map.get(e.getRealmId());
            AppClientResponse result = AppClientConvert.INSTANCE.toAppClientResponse(e);
            result.setRealmName(infoResponse.getName());
            return result;
        }).toList();
    }

    @Override
    public void syncClientsForApp(App app, List<AppClientRequest> requests) {
        List<AppClient> existingClients = appClientMapper.selectList(new LambdaQueryWrapper<AppClient>()
                .eq(AppClient::getAppId, app.getId()));
        Map<Long, AppClient> existingClientMap = existingClients.stream()
                .collect(Collectors.toMap(AppClient::getId, Function.identity()));

        syncAppCodeSnapshot(app, existingClients);
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }

        Set<String> requestClientCodes = new HashSet<>();
        for (AppClientRequest request : requests) {
            if (!requestClientCodes.add(request.getClientCode())) {
                throw new BizException(AppErrorCode.APP_CLIENT_ID_EXISTS);
            }

            if (request.getId() == null) {
                createClientForApp(app, request);
            } else {
                AppClient client = existingClientMap.get(request.getId());
                if (client == null) {
                    throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
                }
                updateClientForApp(app, client, request);
            }
        }
    }

    @Override
    public AppClient detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long appId, AppClientRequest request) {
        App app = findEnabledApp(appId);
        createClientForApp(app, request);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppClientRequest request) {
        AppClient client = findById(id);
        App app = findEnabledApp(client.getAppId());
        updateClientForApp(app, client, request);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppClient client = findById(id);
        findEnabledApp(client.getAppId());
        client.setStatus(StatusEnum.NORMAL.getCode());
        appClientMapper.updateById(client);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppClient client = findById(id);
        client.setStatus(StatusEnum.DISABLED.getCode());
        appClientMapper.updateById(client);
        return Boolean.TRUE;
    }

    private void createClientForApp(App app, AppClientRequest request) {
        validateClientRequest(null, app, request);
        AppClient client = new AppClient();
        BeanUtils.copyProperties(request, client, "id");
        client.setAppId(app.getId());
        client.setAppCode(app.getAppCode());
        appClientMapper.insert(client);
    }

    private void updateClientForApp(App app, AppClient client, AppClientRequest request) {
        validateClientRequest(client.getId(), app, request);
        BeanUtils.copyProperties(request, client, "id", "appId", "appCode", "createTime", "updateTime");
        client.setAppCode(app.getAppCode());
        appClientMapper.updateById(client);
    }

    private void syncAppCodeSnapshot(App app, List<AppClient> existingClients) {
        for (AppClient client : existingClients) {
            if (!Objects.equals(client.getAppCode(), app.getAppCode())) {
                client.setAppCode(app.getAppCode());
                appClientMapper.updateById(client);
            }
        }
    }

    private void validateClientRequest(Long currentId, App app, AppClientRequest request) {
        AppClientType clientType = AppClientType.of(request.getClientType());
        if (clientType == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
//        if (request.getDefaultRealmId() != null) {
//            validateRealm(request.getDefaultRealmId());
//        }

        Long count = appClientMapper.selectCount(new LambdaQueryWrapper<AppClient>()
                .eq(AppClient::getAppCode, app.getAppCode())
                .eq(AppClient::getClientCode, request.getClientCode())
                .ne(currentId != null, AppClient::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_CLIENT_ID_EXISTS);
        }
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

    private AppClient findById(Long id) {
        AppClient client = appClientMapper.selectById(id);
        if (client == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        return client;
    }

    private App findEnabledApp(Long appId) {
        App app = findApp(appId);
        if (!Objects.equals(app.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_DISABLED_DENIED);
        }
        return app;
    }

    private App findApp(Long appId) {
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        return app;
    }

}

