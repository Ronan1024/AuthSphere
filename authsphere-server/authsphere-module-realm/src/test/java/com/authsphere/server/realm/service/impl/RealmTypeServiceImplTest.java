package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.CreateRealmTypeRequest;
import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.error.RealmTypeErrorCode;
import com.authsphere.server.realm.mapper.RealmTypeMapper;
import com.authsphere.server.realm.model.RealmType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 身份域类型服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RealmTypeServiceImplTest {

    @Mock
    private RealmTypeMapper realmTypeMapper;

    @Mock
    private RealmDomain realmDomain;

    @Mock
    private RealmTypeDomain realmTypeDomain;

    @InjectMocks
    private RealmTypeServiceImpl realmTypeService;

    @Test
    void pageShouldDelegateToMapperXml() {
        RealmTypePageRequest request = new RealmTypePageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<RealmTypePageResponse> mapperResult = new Page<>(1, 10);
        when(realmTypeMapper.page(any(Page.class), any(RealmTypePageRequest.class))).thenReturn(mapperResult);

        Page<RealmTypePageResponse> result = realmTypeService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void listAllShouldDelegateToMapperXml() {
        RealmTypePageResponse response = new RealmTypePageResponse();
        response.setCode("realm-type");
        when(realmTypeMapper.listAll()).thenReturn(List.of(response));

        List<RealmTypePageResponse> result = realmTypeService.listAll();

        assertEquals(1, result.size());
        assertEquals("realm-type", result.getFirst().getCode());
    }

    @Test
    void createShouldInsertRealmType() {
        Boolean result = realmTypeService.create(createRequest());

        assertTrue(result);
        ArgumentCaptor<RealmType> captor = ArgumentCaptor.forClass(RealmType.class);
        verify(realmTypeMapper).insert(captor.capture());
        assertEquals("realm-type", captor.getValue().getCode());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void editShouldUpdateExistingRealmType() {
        RealmType realmType = createRealmType();
        when(realmTypeMapper.selectById(1L)).thenReturn(realmType);

        CreateRealmTypeRequest request = createRequest();
        request.setName("身份域分类");
        Boolean result = realmTypeService.edit(1L, request);

        assertTrue(result);
        ArgumentCaptor<RealmType> captor = ArgumentCaptor.forClass(RealmType.class);
        verify(realmTypeMapper).updateById(captor.capture());
        assertSame(realmType, captor.getValue());
        assertEquals("身份域分类", captor.getValue().getName());
    }

    @Test
    void editStatusShouldToggleNormalToDisabled() {
        RealmType realmType = createRealmType();
        realmType.setStatus(StatusEnum.NORMAL.getCode());
        when(realmTypeMapper.selectById(1L)).thenReturn(realmType);

        Boolean result = realmTypeService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<RealmType> captor = ArgumentCaptor.forClass(RealmType.class);
        verify(realmTypeMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void editShouldThrowBizExceptionWhenRealmTypeNotExists() {
        when(realmTypeMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> realmTypeService.edit(1L, createRequest()));

        assertEquals(RealmErrorCode.TYPE_CATEGORY_DATA_ERROR.getCode(), exception.getCode());
    }

    private CreateRealmTypeRequest createRequest() {
        CreateRealmTypeRequest request = new CreateRealmTypeRequest();
        request.setCode("realm-type");
        request.setName("身份域类型");
        request.setDescription("测试分类");
        request.setSystemBuiltin(Boolean.TRUE);
        request.setEditable(Boolean.TRUE);
        return request;
    }

    private RealmType createRealmType() {
        RealmType realmType = new RealmType();
        realmType.setId(1L);
        realmType.setCode("realm-type");
        realmType.setName("身份域类型");
        realmType.setSystemBuiltin(Boolean.TRUE);
        realmType.setEditable(Boolean.TRUE);
        realmType.setStatus(StatusEnum.NORMAL.getCode());
        return realmType;
    }
}
