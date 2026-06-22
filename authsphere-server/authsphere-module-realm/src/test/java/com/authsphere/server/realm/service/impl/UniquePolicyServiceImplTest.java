package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.realm.dto.UniquePolicyPageRequest;
import com.authsphere.server.realm.dto.UniquePolicyRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.mapper.UniquePolicyMapper;
import com.authsphere.server.realm.model.UniquePolicy;
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
 * 账号唯一性规则模板服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UniquePolicyServiceImplTest {

    @Mock
    private UniquePolicyMapper uniquePolicyMapper;

    @InjectMocks
    private UniquePolicyServiceImpl uniquePolicyService;

    @Test
    void pageShouldDelegateToMapperXml() {
        Page<UniquePolicyResponse> mapperResult = new Page<>(1, 10);
        when(uniquePolicyMapper.page(any(Page.class), any(UniquePolicyPageRequest.class))).thenReturn(mapperResult);

        UniquePolicyPageRequest request = new UniquePolicyPageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<UniquePolicyResponse> result = uniquePolicyService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void createShouldInsertPolicyTemplate() {
        when(uniquePolicyMapper.selectCount(any())).thenReturn(0L);

        UniquePolicyResponse response = uniquePolicyService.create(createRequest());

        ArgumentCaptor<UniquePolicy> captor = ArgumentCaptor.forClass(UniquePolicy.class);
        verify(uniquePolicyMapper).insert(captor.capture());
        assertEquals("unique-default", captor.getValue().getCode());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
        assertEquals(captor.getValue().getId(), response.getId());
    }

    private UniquePolicyRequest createRequest() {
        UniquePolicyRequest request = new UniquePolicyRequest();
        request.setCode("unique-default");
        request.setName("默认账号唯一性规则");
        request.setUsernameUnique(Boolean.TRUE);
        request.setEmailUnique(Boolean.TRUE);
        request.setMobileUnique(Boolean.TRUE);
        request.setCaseSensitive(Boolean.FALSE);
        request.setAllowEmailReuse(Boolean.FALSE);
        request.setAllowMobileReuse(Boolean.FALSE);
        request.setDescription("测试账号唯一性规则");
        return request;
    }

    private UniquePolicy createPolicy() {
        UniquePolicy policy = new UniquePolicy();
        policy.setId(10L);
        policy.setCode("unique-default");
        policy.setName("默认账号唯一性规则");
        policy.setStatus(StatusEnum.NORMAL.getCode());
        return policy;
    }
}
