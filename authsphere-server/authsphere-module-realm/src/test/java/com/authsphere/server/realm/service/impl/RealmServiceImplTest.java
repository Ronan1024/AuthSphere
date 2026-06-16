package com.authsphere.server.realm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.AuthPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
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
    private com.authsphere.server.realm.mapper.LoginPageMapper loginPageMapper;

    @Mock
    private com.authsphere.server.realm.service.LoginPageService loginPageService;

    @Mock
    private com.authsphere.server.realm.mapper.AuthPolicyMapper authPolicyMapper;

    @Mock
    private com.authsphere.server.realm.service.AuthPolicyService authPolicyService;

    @InjectMocks
    private RealmServiceImpl realmService;

    @Test
    void createShouldInsertRealmWhenDatabaseHasNoSameCode() {
        CreateRealmRequest request = createRequest("main", "主身份域");
        when(realmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

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
        assertEquals(10L, savedRealm.getPasswordPolicy());
        assertEquals("测试身份域", savedRealm.getDescription());
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
        assertEquals(10L, updatedRealm.getPasswordPolicy());
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

        com.authsphere.server.realm.model.RealmType mockType = new com.authsphere.server.realm.model.RealmType();
        mockType.setId(10L);
        mockType.setName("测试类型");
        when(realmTypeDomain.findByIdList(any(List.class))).thenReturn(List.of(mockType));

        Page<RealmPageResponse> result = realmService.page(request);

        assertSame(mapperResult, result);
        ArgumentCaptor<IPage<RealmPageResponse>> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        verify(realmMapper).page(pageCaptor.capture(), same(request));
        IPage<RealmPageResponse> mapperPage = pageCaptor.getValue();
        assertEquals(2, mapperPage.getCurrent());
        assertEquals(20, mapperPage.getSize());
    }

    private CreateRealmRequest createRequest(String code, String name) {
        CreateRealmRequest request = new CreateRealmRequest();
        request.setCode(code);
        request.setName(name);
        request.setRegisterEnabled(Boolean.TRUE);
        request.setSsoEnabled(Boolean.FALSE);
        request.setPasswordPolicy(10L);
        request.setDescription("测试身份域");
        return request;
    }
}
