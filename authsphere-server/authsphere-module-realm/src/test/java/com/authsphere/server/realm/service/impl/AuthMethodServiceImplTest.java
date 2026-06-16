package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.domain.AuthMethodDomain;
import com.authsphere.server.realm.dto.AuthMethodRequest;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.dto.AuthMethodTemplateResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证方式服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthMethodServiceImplTest {

    @Mock
    private AuthMethodMapper authMethodMapper;

    private AuthMethodServiceImpl authMethodService;

    @BeforeEach
    void setUp() {
        AuthMethodDomain authMethodDomain = new AuthMethodDomain(authMethodMapper, new ObjectMapper());
        authMethodService = new AuthMethodServiceImpl(authMethodMapper, authMethodDomain);
    }

    @Test
    void deleteShouldRejectSystemBuiltinAuthMethod() {
        AuthMethod method = createBuiltinMethod("password_login", "{}");
        when(authMethodMapper.selectById(1L)).thenReturn(method);

        BizException exception = assertThrows(BizException.class, () -> authMethodService.delete(1L));

        assertEquals(RealmErrorCode.AUTH_METHOD_SYSTEM_BUILTIN.getCode(), exception.getCode());
        verify(authMethodMapper, never()).deleteById(1L);
    }

    @Test
    void updateShouldRejectFormSchemaForLockedBuiltinTemplate() {
        AuthMethod method = createBuiltinMethod("password_login", "{\"passwordPolicy\":\"default\"}");
        when(authMethodMapper.selectById(1L)).thenReturn(method);

        AuthMethodRequest request = createRequest("password_login");
        request.setParams(Map.of("formSchema", List.of(Map.of("code", "username", "label", "用户名"))));

        BizException exception = assertThrows(BizException.class, () -> authMethodService.update(1L, request));

        assertEquals(RealmErrorCode.AUTH_METHOD_TEMPLATE_LOCKED.getCode(), exception.getCode());
        verify(authMethodMapper, never()).updateById(any(AuthMethod.class));
    }

    @Test
    void createShouldRejectExtendedAuthMethodWithoutFormSchema() {
        AuthMethodRequest request = createRequest("wechat_login");
        request.setParams(Map.of());

        BizException exception = assertThrows(BizException.class, () -> authMethodService.create(request));

        assertEquals(RealmErrorCode.AUTH_METHOD_FORM_SCHEMA_REQUIRED.getCode(), exception.getCode());
        verify(authMethodMapper, never()).insert(any(AuthMethod.class));
    }

    @Test
    void createShouldPersistExtendedAuthMethodFormSchema() {
        when(authMethodMapper.countByCode("wechat_login", null)).thenReturn(0);

        AuthMethodRequest request = createRequest("wechat_login");
        request.setParams(Map.of("formSchema", List.of(
                field("appId", "AppId"),
                field("appSecret", "AppSecret")
        )));

        authMethodService.create(request);

        ArgumentCaptor<AuthMethod> captor = ArgumentCaptor.forClass(AuthMethod.class);
        verify(authMethodMapper).insert(captor.capture());
        AuthMethod saved = captor.getValue();
        assertEquals("wechat_login", saved.getCode());
        assertFalse(saved.getSystemBuiltin());
        assertEquals("主登录,账号绑定校验", saved.getPositions());
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"formSchema\""));
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"appSecret\""));
    }

    @Test
    void createShouldPersistPasswordTemplateParamsForCustomCode() {
        when(authMethodMapper.countByCode("tenant_password_login", null)).thenReturn(0);

        AuthMethodRequest request = createRequest("tenant_password_login");
        request.setParams(Map.of(
                "passwordPolicy", "default",
                "maxRetries", 5,
                "lockDuration", 30
        ));

        authMethodService.create(request);

        ArgumentCaptor<AuthMethod> captor = ArgumentCaptor.forClass(AuthMethod.class);
        verify(authMethodMapper).insert(captor.capture());
        AuthMethod saved = captor.getValue();
        assertEquals("tenant_password_login", saved.getCode());
        assertFalse(saved.getSystemBuiltin());
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"passwordPolicy\""));
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"maxRetries\""));
        org.junit.jupiter.api.Assertions.assertFalse(saved.getParamsJson().contains("\"formSchema\""));
    }

    @Test
    void createShouldPersistClientCredentialsTemplateParamsForCustomCode() {
        when(authMethodMapper.countByCode("tenant_api_client", null)).thenReturn(0);

        AuthMethodRequest request = createRequest("tenant_api_client");
        request.setPositions(List.of("接口认证"));
        request.setParams(Map.of(
                "tokenTtl", 365,
                "signAlg", "HS256",
                "allowScope", "iam:token, api:read"
        ));

        authMethodService.create(request);

        ArgumentCaptor<AuthMethod> captor = ArgumentCaptor.forClass(AuthMethod.class);
        verify(authMethodMapper).insert(captor.capture());
        AuthMethod saved = captor.getValue();
        assertEquals("tenant_api_client", saved.getCode());
        assertEquals("接口认证", saved.getPositions());
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"tokenTtl\""));
        org.junit.jupiter.api.Assertions.assertTrue(saved.getParamsJson().contains("\"signAlg\""));
    }

    @Test
    void pageShouldExposeMaskedParamsForFrontendSummary() {
        AuthMethodResponse response = new AuthMethodResponse();
        response.setId(1L);
        response.setCode("oidc_login");
        response.setName("OIDC 登录");
        response.setPositionsText("主登录");
        response.setParamsJson("{\"clientId\":\"authsphere\",\"clientSecret\":\"raw-secret\"}");
        Page<AuthMethodResponse> mapperPage = new Page<>(1, 10);
        mapperPage.setRecords(List.of(response));
        mapperPage.setTotal(1);
        when(authMethodMapper.page(any(Page.class), any())).thenReturn(mapperPage);

        Page<AuthMethodResponse> result = authMethodService.page(createPageRequest());

        AuthMethodResponse first = result.getRecords().get(0);
        assertEquals(List.of("主登录"), first.getPositions());
        assertEquals("authsphere", first.getParams().get("clientId"));
        assertEquals("********", first.getParams().get("clientSecret"));
    }

    @Test
    void listTemplatesShouldReturnOnlyBuiltinDefaultNameAndCode() {
        List<AuthMethodTemplateResponse> templates = authMethodService.listTemplates();

        assertEquals(3, templates.size());
        assertEquals("password", templates.get(0).getTemplate());
        assertEquals("账号密码", templates.get(0).getDefaultName());
        assertEquals("password_login", templates.get(0).getDefaultCode());
        assertEquals("totp", templates.get(1).getTemplate());
        assertEquals("TOTP 动态口令", templates.get(1).getDefaultName());
        assertEquals("totp_login", templates.get(1).getDefaultCode());
        assertEquals("client", templates.get(2).getTemplate());
        assertEquals("Client Credentials", templates.get(2).getDefaultName());
        assertEquals("client_credentials", templates.get(2).getDefaultCode());
    }

    private AuthMethodRequest createRequest(String code) {
        AuthMethodRequest request = new AuthMethodRequest();
        request.setCode(code);
        request.setName("测试认证方式");
        request.setPositions(List.of("主登录", "账号绑定校验"));
        request.setApplicableRange("全部");
        request.setStatus(1);
        request.setSortNo(10);
        request.setDescription("测试");
        request.setParams(new LinkedHashMap<>());
        return request;
    }

    private com.authsphere.server.realm.dto.AuthMethodPageRequest createPageRequest() {
        com.authsphere.server.realm.dto.AuthMethodPageRequest request =
                new com.authsphere.server.realm.dto.AuthMethodPageRequest();
        request.setPage(1);
        request.setSize(10);
        return request;
    }

    private AuthMethod createBuiltinMethod(String code, String paramsJson) {
        AuthMethod method = new AuthMethod();
        method.setId(1L);
        method.setCode(code);
        method.setName("系统认证方式");
        method.setPositions("主登录");
        method.setApplicableRange("全部");
        method.setParamsJson(paramsJson);
        method.setSortNo(10);
        method.setSystemBuiltin(Boolean.TRUE);
        method.setStatus(1);
        method.setDeleted(Boolean.FALSE);
        return method;
    }

    private Map<String, Object> field(String code, String label) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("code", code);
        field.put("label", label);
        field.put("type", "text");
        field.put("required", Boolean.TRUE);
        field.put("secret", Boolean.FALSE);
        return field;
    }
}
