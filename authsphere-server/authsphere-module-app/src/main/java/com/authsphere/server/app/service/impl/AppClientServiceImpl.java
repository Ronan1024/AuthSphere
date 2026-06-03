package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppClientPageRequest;
import com.authsphere.server.app.dto.AppClientRequest;
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

import java.util.List;
import java.util.Objects;

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

    @Override
    public Page<AppClient> page(AppClientPageRequest request) {
        Page<AppClient> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<AppClient> wrapper = new LambdaQueryWrapper<AppClient>()
                .eq(request.getAppId() != null, AppClient::getAppId, request.getAppId())
                .eq(request.getAppCode() != null, AppClient::getAppCode, request.getAppCode())
                .like(request.getClientCode() != null, AppClient::getClientCode, request.getClientCode())
                .like(request.getClientName() != null, AppClient::getClientName, request.getClientName())
                .eq(request.getClientType() != null, AppClient::getClientType, request.getClientType())
                .eq(request.getStatus() != null, AppClient::getStatus, request.getStatus())
                .orderByDesc(AppClient::getCreateTime);
        return appClientMapper.selectPage(page, wrapper);
    }

    @Override
    public List<AppClient> listByApp(Long appId) {
        findEnabledApp(appId);
        return appClientMapper.selectList(new LambdaQueryWrapper<AppClient>()
                .eq(AppClient::getAppId, appId)
                .orderByDesc(AppClient::getCreateTime));
    }

    @Override
    public AppClient detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long appId, AppClientRequest request) {
        validateClientRequest(null, appId, request);
        App app = findEnabledApp(appId);
        AppClient client = new AppClient();
        BeanUtils.copyProperties(request, client);
        client.setAppId(app.getId());
        client.setAppCode(app.getAppCode());
        client.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appClientMapper.insert(client);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppClientRequest request) {
        AppClient client = findById(id);
        validateClientRequest(id, client.getAppId(), request);
        BeanUtils.copyProperties(request, client, "id", "appId", "appCode", "createTime", "updateTime");
        client.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appClientMapper.updateById(client);
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

    private void validateClientRequest(Long currentId, Long appId, AppClientRequest request) {
        AppClientType clientType = AppClientType.of(request.getClientType());
        if (clientType == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        App app = findEnabledApp(appId);
        if (request.getDefaultRealmId() != null) {
            validateRealm(request.getDefaultRealmId());
        }

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
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        if (!Objects.equals(app.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_DISABLED_DENIED);
        }
        return app;
    }

}

