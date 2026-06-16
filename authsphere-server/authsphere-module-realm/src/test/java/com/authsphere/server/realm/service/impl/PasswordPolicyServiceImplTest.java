package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyPageRequest;
import com.authsphere.server.realm.dto.PasswordPolicyRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.PasswordPolicyMapper;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.PasswordPolicy;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 密码策略模板服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PasswordPolicyServiceImplTest {

    @Mock
    private PasswordPolicyMapper passwordPolicyMapper;

    @Mock
    private RealmMapper realmMapper;

    @InjectMocks
    private PasswordPolicyServiceImpl passwordPolicyService;

    @Test
    void pageShouldReturnPolicyTemplates() {
        PasswordPolicy policy = createPolicy();
        PasswordPolicyListResponse response = new PasswordPolicyListResponse();
        response.setId(policy.getId());
        response.setCode(policy.getCode());
        response.setName(policy.getName());
        Page<PasswordPolicyListResponse> mapperResult = new Page<>(1, 10);
        mapperResult.setRecords(java.util.List.of(response));
        when(passwordPolicyMapper.page(any(Page.class), any(PasswordPolicyPageRequest.class))).thenReturn(mapperResult);

        PasswordPolicyPageRequest request = new PasswordPolicyPageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setCode("default");
        request.setName("默认");
        request.setStatus(StatusEnum.NORMAL.getCode());
        Page<PasswordPolicyListResponse> result = passwordPolicyService.page(request);

        assertEquals(1, result.getRecords().size());
        assertEquals("default", result.getRecords().getFirst().getCode());
        assertEquals("默认密码策略", result.getRecords().getFirst().getName());
    }

    @Test
    void createShouldInsertPolicyTemplate() {
        when(passwordPolicyMapper.selectCount(any())).thenReturn(0L);

        PasswordPolicyResponse response = passwordPolicyService.create(createRequest());

        ArgumentCaptor<PasswordPolicy> captor = ArgumentCaptor.forClass(PasswordPolicy.class);
        verify(passwordPolicyMapper).insert(captor.capture());
        PasswordPolicy savedPolicy = captor.getValue();
        assertEquals("default", savedPolicy.getCode());
        assertEquals("默认密码策略", savedPolicy.getName());
        assertEquals(StatusEnum.NORMAL.getCode(), savedPolicy.getStatus());
        assertEquals(savedPolicy.getId(), response.getId());
    }

    @Test
    void createShouldThrowBizExceptionWhenCodeExists() {
        when(passwordPolicyMapper.selectCount(any())).thenReturn(1L);

        BizException exception = assertThrows(BizException.class,
                () -> passwordPolicyService.create(createRequest()));

        assertEquals(RealmErrorCode.PASSWORD_POLICY_CODE_EXISTS.getCode(), exception.getCode());
        verify(passwordPolicyMapper, never()).insert(any(PasswordPolicy.class));
    }

    @Test
    void updateShouldModifyExistingPolicyTemplate() {
        PasswordPolicy policy = createPolicy();
        when(passwordPolicyMapper.selectById(10L)).thenReturn(policy);
        when(passwordPolicyMapper.selectCount(any())).thenReturn(0L);

        PasswordPolicyRequest request = createRequest();
        request.setName("强密码策略");
        request.setMinLength(12);

        PasswordPolicyResponse response = passwordPolicyService.update(10L, request);

        ArgumentCaptor<PasswordPolicy> captor = ArgumentCaptor.forClass(PasswordPolicy.class);
        verify(passwordPolicyMapper).updateById(captor.capture());
        assertSame(policy, captor.getValue());
        assertEquals("强密码策略", policy.getName());
        assertEquals(12, policy.getMinLength());
        assertEquals(10L, response.getId());
    }

    @Test
    void createShouldThrowBizExceptionWhenMinLengthGreaterThanMaxLength() {
        PasswordPolicyRequest request = createRequest();
        request.setMinLength(32);
        request.setMaxLength(16);

        BizException exception = assertThrows(BizException.class,
                () -> passwordPolicyService.create(request));

        assertEquals(RealmErrorCode.PASSWORD_POLICY_PARAM_ERROR.getCode(), exception.getCode());
        verify(passwordPolicyMapper, never()).insert(any(PasswordPolicy.class));
    }

    @Test
    void editStatusShouldToggleNormalPolicyToDisabled() {
        PasswordPolicy policy = createPolicy();
        policy.setStatus(StatusEnum.NORMAL.getCode());
        when(passwordPolicyMapper.selectById(10L)).thenReturn(policy);

        Boolean result = passwordPolicyService.editStatus(10L);

        assertTrue(result);
        ArgumentCaptor<PasswordPolicy> captor = ArgumentCaptor.forClass(PasswordPolicy.class);
        verify(passwordPolicyMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void bindRealmPolicyShouldUpdateRealmPasswordPolicy() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);

        PasswordPolicy policy = createPolicy();
        policy.setStatus(StatusEnum.NORMAL.getCode());
        when(passwordPolicyMapper.selectById(10L)).thenReturn(policy);

        Boolean result = passwordPolicyService.bindRealmPolicy(1L, 10L);

        assertTrue(result);
        ArgumentCaptor<Realm> captor = ArgumentCaptor.forClass(Realm.class);
        verify(realmMapper).updateById(captor.capture());
        assertSame(realm, captor.getValue());
        assertEquals(10L, captor.getValue().getPasswordPolicy());
    }

    @Test
    void bindRealmPolicyShouldRejectDisabledPolicy() {
        Realm realm = new Realm();
        realm.setId(1L);
        when(realmMapper.selectById(1L)).thenReturn(realm);

        PasswordPolicy policy = createPolicy();
        policy.setStatus(StatusEnum.DISABLED.getCode());
        when(passwordPolicyMapper.selectById(10L)).thenReturn(policy);

        BizException exception = assertThrows(BizException.class,
                () -> passwordPolicyService.bindRealmPolicy(1L, 10L));

        assertEquals(RealmErrorCode.PASSWORD_POLICY_DATA_ERROR.getCode(), exception.getCode());
        verify(realmMapper, never()).updateById(any(Realm.class));
    }

    @Test
    void getRealmPolicyShouldReturnBoundPolicyTemplate() {
        Realm realm = new Realm();
        realm.setId(1L);
        realm.setPasswordPolicy(10L);
        when(realmMapper.selectById(1L)).thenReturn(realm);
        when(passwordPolicyMapper.selectById(10L)).thenReturn(createPolicy());

        PasswordPolicyResponse response = passwordPolicyService.getRealmPolicy(1L);

        assertEquals(10L, response.getId());
        assertEquals("default", response.getCode());
    }

    private PasswordPolicyRequest createRequest() {
        PasswordPolicyRequest request = new PasswordPolicyRequest();
        request.setCode("default");
        request.setName("默认密码策略");
        request.setMinLength(8);
        request.setMaxLength(64);
        request.setRequireUppercase(Boolean.TRUE);
        request.setRequireLowercase(Boolean.TRUE);
        request.setRequireDigit(Boolean.TRUE);
        request.setRequireSpecialChar(Boolean.FALSE);
        request.setDisallowUsername(Boolean.TRUE);
        request.setHistoryCount(5);
        request.setExpireDays(90);
        request.setRetryLimit(5);
        request.setLockMinutes(30);
        request.setDescription("测试策略");
        return request;
    }

    private PasswordPolicy createPolicy() {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setId(10L);
        policy.setCode("default");
        policy.setName("默认密码策略");
        policy.setMinLength(8);
        policy.setMaxLength(64);
        policy.setRequireUppercase(Boolean.TRUE);
        policy.setRequireLowercase(Boolean.TRUE);
        policy.setRequireDigit(Boolean.TRUE);
        policy.setRequireSpecialChar(Boolean.FALSE);
        policy.setDisallowUsername(Boolean.TRUE);
        policy.setHistoryCount(5);
        policy.setExpireDays(90);
        policy.setRetryLimit(5);
        policy.setLockMinutes(30);
        policy.setStatus(StatusEnum.NORMAL.getCode());
        policy.setDescription("测试策略");
        return policy;
    }
}
