package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.realm.dto.MfaPolicyPageRequest;
import com.authsphere.server.realm.dto.MfaPolicyRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.mapper.MfaPolicyMapper;
import com.authsphere.server.realm.model.MfaPolicy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MFA 策略模板服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MfaPolicyServiceImplTest {

    @Mock
    private MfaPolicyMapper mfaPolicyMapper;

    @InjectMocks
    private MfaPolicyServiceImpl mfaPolicyService;

    @Test
    void pageShouldDelegateToMapperXml() {
        Page<MfaPolicyResponse> mapperResult = new Page<>(1, 10);
        when(mfaPolicyMapper.page(any(Page.class), any(MfaPolicyPageRequest.class))).thenReturn(mapperResult);

        MfaPolicyPageRequest request = new MfaPolicyPageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<MfaPolicyResponse> result = mfaPolicyService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void createShouldInsertPolicyTemplate() {
        when(mfaPolicyMapper.selectCount(any())).thenReturn(0L);

        MfaPolicyResponse response = mfaPolicyService.create(createRequest());

        ArgumentCaptor<MfaPolicy> captor = ArgumentCaptor.forClass(MfaPolicy.class);
        verify(mfaPolicyMapper).insert(captor.capture());
        assertEquals("mfa-default", captor.getValue().getCode());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
        assertEquals(captor.getValue().getId(), response.getId());
    }

    private MfaPolicyRequest createRequest() {
        MfaPolicyRequest request = new MfaPolicyRequest();
        request.setCode("mfa-default");
        request.setName("默认 MFA 策略");
        request.setRequired(Boolean.TRUE);
        request.setSmsEnabled(Boolean.TRUE);
        request.setEmailEnabled(Boolean.TRUE);
        request.setTotpEnabled(Boolean.TRUE);
        request.setWebauthnEnabled(Boolean.FALSE);
        request.setRememberDeviceEnabled(Boolean.TRUE);
        request.setRememberDeviceDays(30);
        request.setRiskBasedEnabled(Boolean.FALSE);
        request.setDescription("测试 MFA 策略");
        return request;
    }

    private MfaPolicy createPolicy() {
        MfaPolicy policy = new MfaPolicy();
        policy.setId(10L);
        policy.setCode("mfa-default");
        policy.setName("默认 MFA 策略");
        policy.setStatus(StatusEnum.NORMAL.getCode());
        return policy;
    }
}
