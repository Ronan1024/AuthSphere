package com.authsphere.server.app.service.impl;

import com.authsphere.server.api.realm.RealmApi;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.app.convert.AppClientConvert;
import com.authsphere.server.app.domain.AppDomain;
import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.dto.AppClientResponse;
import com.authsphere.server.app.enums.AppClientLoginMode;
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
import com.authsphere.server.realm.service.LoginPageService;
import com.authsphere.server.realm.service.AuthPolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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

    private static final String PROTECTED_SECRET_PREFIX = "ENC:";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppClientMapper appClientMapper;
    private final AppMapper appMapper;
    private final RealmDomain realmDomain;
    private final AppDomain appDomain;
    private final RealmApi realmApi;
    private final LoginPageService loginPageService;
    private final AuthPolicyService authPolicyService;


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
        normalizeLoginConfig(client, request);
        client.setAppId(app.getId());
        client.setAppCode(app.getAppCode());
        client.setClientSecret(resolveCreateClientSecret(request));
        appClientMapper.insert(client);
    }

    private void updateClientForApp(App app, AppClient client, AppClientRequest request) {
        validateClientRequest(client.getId(), app, request);
        String originalClientSecret = client.getClientSecret();
        BeanUtils.copyProperties(request, client, "id", "appId", "appCode", "createTime", "updateTime");
        normalizeLoginConfig(client, request);
        client.setAppCode(app.getAppCode());
        client.setClientSecret(resolveUpdateClientSecret(originalClientSecret, request));
        appClientMapper.updateById(client);
    }

    private String resolveCreateClientSecret(AppClientRequest request) {
        String rawSecret = StringUtils.hasText(request.getClientSecret())
                ? request.getClientSecret()
                : generateClientSecret();
        return protectClientSecret(rawSecret);
    }

    private String resolveUpdateClientSecret(String originalClientSecret, AppClientRequest request) {
        if (!StringUtils.hasText(request.getClientSecret())) {
            return originalClientSecret;
        }
        return protectClientSecret(request.getClientSecret());
    }

    private String generateClientSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "sec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String protectClientSecret(String clientSecret) {
        if (!StringUtils.hasText(clientSecret) || clientSecret.startsWith(PROTECTED_SECRET_PREFIX)) {
            return clientSecret;
        }
        // 这里先保留统一保护入口，后续接入 KMS/AES 后只需要替换本方法实现。
        return PROTECTED_SECRET_PREFIX + Base64.getEncoder()
                .encodeToString(clientSecret.getBytes(StandardCharsets.UTF_8));
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
        AppClientLoginMode loginMode = resolveLoginMode(clientType, request);
        if (loginMode == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        if (loginMode == AppClientLoginMode.IAM_HOSTED && request.getLoginPageId() != null) {
            loginPageService.validateEnabled(request.getLoginPageId());
        }
        if (request.getAuthPolicyId() != null) {
            authPolicyService.validateEnabled(request.getAuthPolicyId());
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

    private AppClientLoginMode resolveLoginMode(AppClientType clientType, AppClientRequest request) {
        AppClientLoginMode loginMode = AppClientLoginMode.of(request.getLoginMode());
        if (loginMode == null) {
            return null;
        }
        if (clientType == AppClientType.OPEN_API || clientType == AppClientType.SERVICE) {
            return AppClientLoginMode.SERVICE;
        }
        return loginMode;
    }

    private void normalizeLoginConfig(AppClient client, AppClientRequest request) {
        AppClientType clientType = AppClientType.of(request.getClientType());
        AppClientLoginMode loginMode = resolveLoginMode(clientType, request);
        client.setLoginMode(loginMode.name());
        if (loginMode != AppClientLoginMode.IAM_HOSTED) {
            client.setLoginPageId(null);
        }
        if (loginMode != AppClientLoginMode.EXTERNAL_REDIRECT) {
            client.setExternalLoginUrl(null);
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
