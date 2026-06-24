package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.api.realm.RealmApi;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.service.AuthPolicyService;
import com.authsphere.server.realm.service.LoginPageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 应用客户端服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppClientServiceImplTest {

    @Mock
    private AppClientMapper appClientMapper;

    @Mock
    private AppMapper appMapper;

    @Mock
    private RealmDomain realmDomain;

    @Mock
    private RealmApi realmApi;

    @Mock
    private LoginPageService loginPageService;

    @Mock
    private AuthPolicyService authPolicyService;

    @InjectMocks
    private AppClientServiceImpl appClientService;

    @Test
    void syncClientsForAppShouldCreateAndUpdateClientsWithoutDeletingMissingClients() {
        App app = new App();
        app.setId(10L);
        app.setAppCode("new_app");

        AppClient existingClient = createClient(100L, 10L, "old_app", "admin_web", "旧管理端");
        AppClient untouchedClient = createClient(101L, 10L, "old_app", "open_api", "开放接口");
        when(appClientMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existingClient, untouchedClient));
        when(appClientMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        AppClientRequest updateRequest = createRequest(100L, "admin_console", "管理控制台");
        AppClientRequest createRequest = createRequest(null, "merchant_web", "商户端");

        appClientService.syncClientsForApp(app, List.of(updateRequest, createRequest));

        ArgumentCaptor<AppClient> insertCaptor = ArgumentCaptor.forClass(AppClient.class);
        verify(appClientMapper).insert(insertCaptor.capture());
        AppClient insertedClient = insertCaptor.getValue();
        assertEquals(10L, insertedClient.getAppId());
        assertEquals("new_app", insertedClient.getAppCode());
        assertEquals("merchant_web", insertedClient.getClientCode());
        assertEquals("商户端", insertedClient.getClientName());
        assertTrue(insertedClient.getClientSecret().startsWith("ENC:"));

        ArgumentCaptor<AppClient> updateCaptor = ArgumentCaptor.forClass(AppClient.class);
        verify(appClientMapper, org.mockito.Mockito.times(3)).updateById(updateCaptor.capture());
        List<AppClient> updatedClients = updateCaptor.getAllValues();
        assertTrue(updatedClients.stream().anyMatch(client ->
                client.getId().equals(100L)
                        && "new_app".equals(client.getAppCode())
                        && "admin_console".equals(client.getClientCode())
                        && "管理控制台".equals(client.getClientName())
                        && "old_secret".equals(client.getClientSecret())));
        assertTrue(updatedClients.stream().anyMatch(client ->
                client.getId().equals(101L)
                        && "new_app".equals(client.getAppCode())
                        && "open_api".equals(client.getClientCode())));
    }

    @Test
    void editShouldRotateClientSecretWhenRequestContainsSecret() {
        App app = new App();
        app.setId(10L);
        app.setAppCode("authsphere");
        app.setStatus(StatusEnum.NORMAL.getCode());

        AppClient existingClient = createClient(100L, 10L, "authsphere", "open_api", "开放接口");
        existingClient.setClientSecret("old_secret");
        when(appClientMapper.selectById(100L)).thenReturn(existingClient);
        when(appMapper.selectById(10L)).thenReturn(app);
        when(appClientMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        AppClientRequest request = createRequest(null, "open_api", "开放接口");
        request.setClientSecret("new_secret");

        appClientService.edit(100L, request);

        ArgumentCaptor<AppClient> updateCaptor = ArgumentCaptor.forClass(AppClient.class);
        verify(appClientMapper).updateById(updateCaptor.capture());
        assertEquals("ENC:bmV3X3NlY3JldA==", updateCaptor.getValue().getClientSecret());
    }

    @Test
    void createShouldPersistClientLoginModeAndClearLoginPageWhenApiOnly() {
        App app = new App();
        app.setId(10L);
        app.setAppCode("authsphere");
        app.setStatus(StatusEnum.NORMAL.getCode());
        when(appMapper.selectById(10L)).thenReturn(app);
        when(appClientMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        AppClientRequest request = createRequest(null, "customer_api", "客户自有登录");
        request.setLoginMode("API_ONLY");
        request.setLoginPageId(200L);
        request.setExternalLoginUrl("https://customer.example.com/login");

        appClientService.create(10L, request);

        ArgumentCaptor<AppClient> insertCaptor = ArgumentCaptor.forClass(AppClient.class);
        verify(appClientMapper).insert(insertCaptor.capture());
        AppClient insertedClient = insertCaptor.getValue();
        assertEquals("API_ONLY", insertedClient.getLoginMode());
        assertEquals(null, insertedClient.getLoginPageId());
        assertEquals(null, insertedClient.getExternalLoginUrl());
    }

    private AppClient createClient(Long id, Long appId, String appCode, String clientCode, String clientName) {
        AppClient client = new AppClient();
        client.setId(id);
        client.setAppId(appId);
        client.setAppCode(appCode);
        client.setClientCode(clientCode);
        client.setClientName(clientName);
        client.setClientType(1);
        client.setClientSecret("old_secret");
        client.setStatus(StatusEnum.NORMAL.getCode());
        return client;
    }

    private AppClientRequest createRequest(Long id, String clientCode, String clientName) {
        AppClientRequest request = new AppClientRequest();
        request.setId(id);
        request.setClientCode(clientCode);
        request.setClientName(clientName);
        request.setClientType(1);
        request.setStatus(StatusEnum.NORMAL.getCode());
        return request;
    }
}
