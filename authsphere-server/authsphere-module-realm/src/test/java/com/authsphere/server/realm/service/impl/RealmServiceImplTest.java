package com.authsphere.server.realm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmDetailResponse;
import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.domain.AuthMethodDomain;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.mapper.RealmAuthMethodRelMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.RealmAuthMethodRel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Realm 服务单元测试。
 *
 * @author L.J.Ran
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RealmServiceImplTest {

    @Mock
    private RealmMapper realmMapper;

    @Mock
    private RealmTypeDomain realmTypeDomain;

    @Mock
    private com.authsphere.server.realm.mapper.AuthMethodMapper authMethodMapper;

    @Mock
    private AuthMethodDomain authMethodDomain;

    @Mock
    private RealmAuthMethodRelMapper realmAuthMethodRelMapper;

    @InjectMocks
    private RealmServiceImpl realmService;

    @Test
    void createRequestShouldExposeDirectSecurityFields() {
        CreateRealmRequest request = new CreateRealmRequest();
        request.setAuthMethodIds(List.of(1L, 2L));
        request.setDefaultAuthMethodId(1L);
        request.setMfaAuthMethodId(2L);
        request.setCaptchaMode("threshold");
        request.setCaptchaThreshold(3);

        assertEquals(List.of(1L, 2L), request.getAuthMethodIds());
        assertEquals(1L, request.getDefaultAuthMethodId());
        assertEquals(2L, request.getMfaAuthMethodId());
        assertEquals("threshold", request.getCaptchaMode());
        assertEquals(3, request.getCaptchaThreshold());
    }

    @Test
    void createShouldInsertRealmWhenDatabaseHasNoSameCode() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(realmMapper.insert(any(Realm.class))).thenReturn(1);
        mockRealmType();
        mockAuthMethods();

        Boolean result = realmService.create(request);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).insert(captor.capture());
        Realm savedRealm = captor.getValue();
        assertEquals("main", savedRealm.getCode());
        assertEquals("主身份域", savedRealm.getName());
        assertEquals(Boolean.TRUE, savedRealm.getRegisterEnabled());
        assertEquals(Boolean.FALSE, savedRealm.getSsoEnabled());
        assertEquals(8, savedRealm.getSsoSessionTimeout());
        assertEquals(30, savedRealm.getSsoIdleTimeout());
        assertEquals("enabled", savedRealm.getSsoSingleLogout());
        assertEquals("auto_redirect", savedRealm.getExistingSessionHandler());
        assertEquals("show_app_list", savedRealm.getNoClientIdHandler());
        assertEquals(1L, savedRealm.getDefaultAuthMethodId());
        assertEquals(2L, savedRealm.getMfaAuthMethodId());
        assertEquals("threshold", savedRealm.getCaptchaMode());
        assertEquals(3, savedRealm.getCaptchaThreshold());
        assertEquals("测试身份域", savedRealm.getDescription());
    }

    @Test
    void createShouldDefaultCaptchaModeToNoneWhenMissing() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setCaptchaMode(null);
        request.setCaptchaThreshold(null);
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(realmMapper.insert(any(Realm.class))).thenReturn(1);
        mockRealmType();
        mockAuthMethods();

        Boolean result = realmService.create(request);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).insert(captor.capture());
        assertEquals("none", captor.getValue().getCaptchaMode());
    }

    @Test
    void createShouldRejectInvalidCaptchaMode() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setCaptchaMode("invalid");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();
        mockAuthMethods();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));

        assertTrue(exception.getMessage().contains("图形验证码模式值无效"));
    }

    @Test
    void createShouldRejectInvalidPasswordComplexity() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setPasswordComplexity("invalid");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();
        mockAuthMethods();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));

        assertTrue(exception.getMessage().contains("密码复杂度值无效"));
    }

    @Test
    void createShouldRejectInvalidSessionMultiDevice() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setSessionMultiDevice("invalid");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();
        mockAuthMethods();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));

        assertTrue(exception.getMessage().contains("多端会话策略值无效"));
    }

    @Test
    void createShouldRejectWhenDefaultAuthMethodNotInSelectedAuthMethods() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setAuthMethodIds(List.of(1L));
        request.setDefaultAuthMethodId(2L);
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();
        mockAuthMethods();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));

        assertTrue(exception.getMessage().contains("默认认证方式"));
    }

    @Test
    void createShouldThrowBizExceptionWhenCodeExists() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new Realm()));

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));

        assertEquals(RealmErrorCode.REALM_CODE_EXISTS.getCode(), exception.getCode());
        assertEquals(RealmErrorCode.REALM_CODE_EXISTS.getMessage(), exception.getMessage());
        verify(realmMapper, never()).insert(any(Realm.class));
    }

    @Test
    void updateShouldCopyRequestFieldsToExistingRealm() {
        Realm existingRealm = new Realm();
        existingRealm.setId(1L);
        existingRealm.setCode("old");
        existingRealm.setName("旧身份域");
        when(realmMapper.selectById(1L)).thenReturn(existingRealm);
        mockRealmType();
        mockAuthMethods();

        CreateRealmRequest request = createRequest("new", "新身份域");
        Boolean result = realmService.update(1L, request);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).updateById(captor.capture());
        Realm updatedRealm = captor.getValue();
        assertSame(existingRealm, updatedRealm);
        assertEquals(1L, updatedRealm.getId());
        assertEquals("new", updatedRealm.getCode());
        assertEquals("新身份域", updatedRealm.getName());
        assertEquals(Boolean.TRUE, updatedRealm.getRegisterEnabled());
        assertEquals(Boolean.FALSE, updatedRealm.getSsoEnabled());
        assertEquals(8, updatedRealm.getSsoSessionTimeout());
        assertEquals(30, updatedRealm.getSsoIdleTimeout());
        assertEquals("enabled", updatedRealm.getSsoSingleLogout());
        assertEquals("auto_redirect", updatedRealm.getExistingSessionHandler());
        assertEquals("show_app_list", updatedRealm.getNoClientIdHandler());
        assertEquals(1L, updatedRealm.getDefaultAuthMethodId());
        assertEquals(2L, updatedRealm.getMfaAuthMethodId());
        assertEquals("测试身份域", updatedRealm.getDescription());
    }

    @Test
    void editStatusShouldDisableNormalRealm() {
        Realm realm = new Realm();
        realm.setId(1L);
        realm.setStatus(StatusEnum.NORMAL.getCode());
        when(realmMapper.selectById(1L)).thenReturn(realm);

        Boolean result = realmService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void editStatusShouldEnableDisabledRealm() {
        Realm realm = new Realm();
        realm.setId(1L);
        realm.setStatus(StatusEnum.DISABLED.getCode());
        when(realmMapper.selectById(1L)).thenReturn(realm);

        Boolean result = realmService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).updateById(captor.capture());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void findByIdShouldReturnRealmWhenExists() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);

        Realm result = realmService.findById(1L);

        assertSame(realm, result);
    }

    @Test
    void findByIdShouldThrowBizExceptionWhenRealmNotExists() {
        when(realmMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> realmService.findById(1L));

        assertEquals(RealmErrorCode.REALM_DATA_ERROR.getCode(), exception.getCode());
        assertEquals(RealmErrorCode.REALM_DATA_ERROR.getMessage(), exception.getMessage());
    }

    @Test
    void detailShouldContainAuthMethodIds() {
        Realm realm = new Realm();
        realm.setId(1L);
        realm.setCode("main");
        realm.setName("主身份域");
        realm.setRealmTypeId(10L);
        realm.setDefaultAuthMethodId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);
        mockRealmType();
        mockAuthMethods();
        RealmAuthMethodRel rel1 = new RealmAuthMethodRel();
        rel1.setRealmId(1L);
        rel1.setAuthMethodId(1L);
        RealmAuthMethodRel rel2 = new RealmAuthMethodRel();
        rel2.setRealmId(1L);
        rel2.setAuthMethodId(2L);
        when(realmAuthMethodRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rel1, rel2), List.of(rel1, rel2));
        when(realmMapper.countAccountReferences(1L)).thenReturn(0);
        when(realmMapper.countClientReferences(1L)).thenReturn(0);

        RealmDetailResponse detail = realmService.detail(1L);
        assertEquals(List.of(1L, 2L), detail.getAuthMethodIds());
    }

    @Test
    void pageShouldDelegateToMapperWithPageAndQueryCondition() {
        RealmPageRequest request = new RealmPageRequest();
        request.setPage(2);
        request.setSize(20);
        request.setCode("main");
        request.setName("主身份域");

        Page<RealmPageResponse> mapperResult = new Page<>(2, 20);
        RealmPageResponse response = new RealmPageResponse();
        response.setId(1L);
        response.setCode("main");
        response.setName("主身份域");
        response.setRealmTypeId(10L);
        mapperResult.setRecords(List.of(response));
        when(realmMapper.page(any(Page.class), same(request))).thenReturn(mapperResult);
        RealmAuthMethodRel rel1 = new RealmAuthMethodRel();
        rel1.setRealmId(1L);
        rel1.setAuthMethodId(1L);
        when(realmAuthMethodRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rel1), List.of(rel1));

        com.authsphere.server.realm.model.RealmType mockType = new com.authsphere.server.realm.model.RealmType();
        mockType.setId(10L);
        mockType.setName("测试类型");
        when(realmTypeDomain.findByIdList(any(List.class))).thenReturn(List.of(mockType));
        AuthMethod method = new AuthMethod();
        method.setId(1L);
        method.setCode("password_login");
        method.setName("账号密码");
        when(authMethodMapper.selectBatchIds(any())).thenReturn(List.of(method));

        Page<RealmPageResponse> result = realmService.page(request);

        assertSame(mapperResult, result);
        ArgumentCaptor<IPage<RealmPageResponse>> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        verify(realmMapper).page(pageCaptor.capture(), same(request));
        IPage<RealmPageResponse> mapperPage = pageCaptor.getValue();
        assertEquals(2, mapperPage.getCurrent());
        assertEquals(20, mapperPage.getSize());
    }

    @Test
    void deleteShouldDeleteRealmWhenNoReferencesExist() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);
        when(realmMapper.countAccountReferences(1L)).thenReturn(0);
        when(realmMapper.countClientReferences(1L)).thenReturn(0);

        Boolean result = realmService.delete(1L);

        assertTrue(result);
        verify(realmMapper).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenAccountReferencesExist() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);
        when(realmMapper.countAccountReferences(1L)).thenReturn(5);

        BizException exception = assertThrows(BizException.class, () -> realmService.delete(1L));
        assertTrue(exception.getMessage().contains("账号"));
        verify(realmMapper, never()).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenClientReferencesExist() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);
        when(realmMapper.countAccountReferences(1L)).thenReturn(0);
        when(realmMapper.countClientReferences(1L)).thenReturn(3);

        BizException exception = assertThrows(BizException.class, () -> realmService.delete(1L));
        assertTrue(exception.getMessage().contains("客户端"));
        verify(realmMapper, never()).deleteById(1L);
    }

    @Test
    void listShouldReturnAllRealms() {
        RealmListResponse r1 = new RealmListResponse();
        r1.setId(1L);
        r1.setName("R1");
        when(realmMapper.listAll()).thenReturn(List.of(r1));
        RealmAuthMethodRel rel1 = new RealmAuthMethodRel();
        rel1.setRealmId(1L);
        rel1.setAuthMethodId(1L);
        when(realmAuthMethodRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rel1));
        AuthMethod method = new AuthMethod();
        method.setId(1L);
        method.setCode("password_login");
        method.setName("账号密码");
        when(authMethodMapper.selectBatchIds(any())).thenReturn(List.of(method));

        List<RealmListResponse> result = realmService.list();

        assertEquals(1, result.size());
        assertEquals("R1", result.get(0).getName());
        assertEquals(1, result.get(0).getAuthMethodList().size());
        assertEquals("password_login", result.get(0).getAuthMethodList().getFirst().getCode());
    }

    @Test
    void createShouldThrowExceptionWhenSsoSingleLogoutIsInvalid() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setSsoSingleLogout("invalid_val");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));
        assertTrue(exception.getMessage().contains("单点退出策略值无效"));
    }

    @Test
    void createShouldThrowExceptionWhenExistingSessionHandlerIsInvalid() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setExistingSessionHandler("invalid_val");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));
        assertTrue(exception.getMessage().contains("已存在会话处理方式值无效"));
    }

    @Test
    void createShouldThrowExceptionWhenNoClientIdHandlerIsInvalid() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        request.setNoClientIdHandler("invalid_val");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        mockRealmType();

        BizException exception = assertThrows(BizException.class, () -> realmService.create(request));
        assertTrue(exception.getMessage().contains("无 client_id 时的处理方式值无效"));
    }

    private void mockRealmType() {
        com.authsphere.server.realm.model.RealmType realmType = new com.authsphere.server.realm.model.RealmType();
        realmType.setId(10L);
        realmType.setName("测试类型");
        lenient().when(realmTypeDomain.findById(10L)).thenReturn(realmType);
        lenient().when(realmTypeDomain.findByIdList(any(List.class))).thenReturn(List.of(realmType));
    }

    private void mockAuthMethods() {
        AuthMethod authMethod1 = new AuthMethod();
        authMethod1.setId(1L);
        authMethod1.setCode("password_login");
        authMethod1.setName("账号密码");
        AuthMethod authMethod2 = new AuthMethod();
        authMethod2.setId(2L);
        authMethod2.setCode("totp_login");
        authMethod2.setName("TOTP");
        List<AuthMethod> methods = List.of(authMethod1, authMethod2);
        lenient().when(authMethodDomain.findAuthMethods(any(List.class))).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return methods.stream().filter(method -> ids.contains(method.getId())).collect(Collectors.toList());
        });
        lenient().when(authMethodMapper.selectBatchIds(any())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return methods.stream().filter(method -> ids.contains(method.getId())).collect(Collectors.toList());
        });
    }

    private CreateRealmRequest createRequest(String code, String name) {
        CreateRealmRequest request = new CreateRealmRequest();
        request.setCode(code);
        request.setName(name);
        request.setRealmTypeId(10L);
        request.setStatus(StatusEnum.NORMAL.getCode());
        request.setRegisterEnabled(Boolean.TRUE);
        request.setSsoEnabled(Boolean.FALSE);
        request.setAuthMethodIds(List.of(1L, 2L));
        request.setDefaultAuthMethodId(1L);
        request.setMfaAuthMethodId(2L);
        request.setCaptchaMode("threshold");
        request.setCaptchaThreshold(3);
        request.setPasswordComplexity("letters_digits");
        request.setSessionMultiDevice("allow");
        request.setDescription("测试身份域");
        return request;
    }
}
