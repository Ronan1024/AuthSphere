package com.authsphere.server.subject.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.dto.SubjectTypeInfoResponse;
import com.authsphere.server.subject.dto.SubjectTypePageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeMapper;
import com.authsphere.server.subject.model.SubjectType;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 主体类型服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubjectTypeServiceImplTest {

    @Mock
    private SubjectTypeMapper subjectTypeMapper;

    @Mock
    private SubjectTypeDomain subjectTypeDomain;

    @InjectMocks
    private SubjectTypeServiceImpl subjectTypeService;

    @Test
    void pageShouldDelegateToMapperXml() {
        SubjectTypePageRequest request = new SubjectTypePageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<SubjectTypeResponse> mapperResult = new Page<>(1, 10);
        when(subjectTypeMapper.page(any(Page.class), any(SubjectTypePageRequest.class))).thenReturn(mapperResult);

        Page<SubjectTypeResponse> result = subjectTypeService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void listAllShouldDelegateToMapperXml() {
        SubjectTypeResponse response = new SubjectTypeResponse();
        response.setCode("user");
        when(subjectTypeMapper.listAll()).thenReturn(List.of(response));

        List<SubjectTypeResponse> result = subjectTypeService.listAll();

        assertEquals(1, result.size());
        assertEquals("user", result.getFirst().getCode());
    }

    @Test
    void detailShouldReturnSubjectTypeResponse() {
        when(subjectTypeDomain.findById(1L)).thenReturn(createSubjectType());

        SubjectTypeInfoResponse result = subjectTypeService.detail(1L);

        assertEquals("user", result.getCode());
        assertEquals("用户主体", result.getName());
        assertEquals(Boolean.TRUE, result.getCanHaveMembers());
    }

    @Test
    void createShouldInsertSubjectType() {
        when(subjectTypeMapper.insert(any(SubjectType.class))).thenReturn(1);

        Boolean result = subjectTypeService.create(createRequest());

        assertTrue(result);
        ArgumentCaptor<SubjectType> captor = ArgumentCaptor.forClass(SubjectType.class);
        verify(subjectTypeMapper).insert(captor.capture());
        assertEquals("user", captor.getValue().getCode());
        assertEquals(Boolean.TRUE, captor.getValue().getCanHaveMembers());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void createShouldThrowBizExceptionWhenCodeExists() {
        doThrow(new BizException(SubjectErrorCode.SUBJECT_TYPE_CODE_EXISTS))
                .when(subjectTypeDomain).checkCodeExists(null, "user");

        BizException exception = assertThrows(BizException.class,
                () -> subjectTypeService.create(createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_CODE_EXISTS.getCode(), exception.getCode());
        verify(subjectTypeMapper, never()).insert(any(SubjectType.class));
    }

    @Test
    void editShouldUpdateExistingSubjectType() {
        SubjectType subjectType = createSubjectType();
        when(subjectTypeDomain.findById(1L)).thenReturn(subjectType);

        SubjectTypeRequest request = createRequest();
        request.setName("员工主体");
        Boolean result = subjectTypeService.edit(1L, request);

        assertTrue(result);
        ArgumentCaptor<SubjectType> captor = ArgumentCaptor.forClass(SubjectType.class);
        verify(subjectTypeMapper).updateById(captor.capture());
        assertSame(subjectType, captor.getValue());
        assertEquals("员工主体", captor.getValue().getName());
    }

    @Test
    void editStatusShouldToggleNormalToDisabled() {
        SubjectType subjectType = createSubjectType();
        subjectType.setStatus(StatusEnum.NORMAL.getCode());
        when(subjectTypeDomain.findById(1L)).thenReturn(subjectType);

        Boolean result = subjectTypeService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<SubjectType> captor = ArgumentCaptor.forClass(SubjectType.class);
        verify(subjectTypeMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void deleteShouldRejectBuiltInSubjectType() {
        SubjectType subjectType = createSubjectType();
        subjectType.setBuiltIn(Boolean.TRUE);
        when(subjectTypeDomain.findById(1L)).thenReturn(subjectType);

        BizException exception = assertThrows(BizException.class,
                () -> subjectTypeService.delete(1L));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_BUILT_IN_DELETE_DENIED.getCode(), exception.getCode());
        verify(subjectTypeMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteShouldRemoveCustomSubjectType() {
        SubjectType subjectType = createSubjectType();
        subjectType.setBuiltIn(Boolean.FALSE);
        when(subjectTypeDomain.findById(1L)).thenReturn(subjectType);

        Boolean result = subjectTypeService.delete(1L);

        assertTrue(result);
        verify(subjectTypeMapper).deleteById(1L);
    }

    @Test
    void editShouldThrowBizExceptionWhenSubjectTypeNotExists() {
        doThrow(new BizException(SubjectErrorCode.SUBJECT_TYPE_DATA_ERROR))
                .when(subjectTypeDomain).findById(1L);

        BizException exception = assertThrows(BizException.class,
                () -> subjectTypeService.edit(1L, createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_DATA_ERROR.getCode(), exception.getCode());
    }

    private SubjectTypeRequest createRequest() {
        SubjectTypeRequest request = new SubjectTypeRequest();
        request.setCode("user");
        request.setName("用户主体");
        request.setCategory("account");
        request.setCanHaveMembers(Boolean.TRUE);
        request.setCanOpenApp(Boolean.TRUE);
        request.setCanAssignRole(Boolean.TRUE);
        request.setCanBeRoot(Boolean.FALSE);
        request.setBuiltIn(Boolean.FALSE);
        request.setDescription("测试主体类型");
        return request;
    }

    private SubjectType createSubjectType() {
        SubjectType subjectType = new SubjectType();
        subjectType.setId(1L);
        subjectType.setCode("user");
        subjectType.setName("用户主体");
        subjectType.setCategory("account");
        subjectType.setCanHaveMembers(Boolean.TRUE);
        subjectType.setCanOpenApp(Boolean.TRUE);
        subjectType.setCanAssignRole(Boolean.TRUE);
        subjectType.setCanBeRoot(Boolean.FALSE);
        subjectType.setBuiltIn(Boolean.FALSE);
        subjectType.setStatus(StatusEnum.NORMAL.getCode());
        return subjectType;
    }
}
