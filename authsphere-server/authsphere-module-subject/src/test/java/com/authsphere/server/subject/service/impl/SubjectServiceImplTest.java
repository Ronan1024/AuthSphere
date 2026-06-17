package com.authsphere.server.subject.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.domain.SubjectDomain;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectMapper;
import com.authsphere.server.subject.model.Subject;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 主体服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubjectServiceImplTest {

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectDomain subjectDomain;

    @Mock
    private SubjectTypeDomain subjectTypeDomain;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    @Test
    void pageShouldDelegateToMapperXml() {
        SubjectPageRequest request = new SubjectPageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<SubjectResponse> mapperResult = new Page<>(1, 10);
        when(subjectMapper.page(any(Page.class), any(SubjectPageRequest.class))).thenReturn(mapperResult);

        Page<SubjectResponse> result = subjectService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void listAllShouldDelegateToMapperXml() {
        SubjectResponse response = new SubjectResponse();
        response.setCode("saas");
        when(subjectMapper.listAll()).thenReturn(List.of(response));

        List<SubjectResponse> result = subjectService.listAll();

        assertEquals(1, result.size());
        assertEquals("saas", result.getFirst().getCode());
    }

    @Test
    void detailShouldReturnMapperDetail() {
        SubjectResponse response = new SubjectResponse();
        response.setCode("saas");
        when(subjectDomain.findById(1L)).thenReturn(createSubject());
        when(subjectMapper.detail(1L)).thenReturn(response);

        SubjectResponse result = subjectService.detail(1L);

        assertSame(response, result);
    }

    @Test
    void createShouldInsertSubjectWithNormalStatus() {
        mockValidReference();

        Boolean result = subjectService.create(createRequest());

        assertTrue(result);
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectMapper).insert(captor.capture());
        assertEquals("saas", captor.getValue().getCode());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void createShouldThrowBizExceptionWhenCodeExists() {
        mockValidReference();
        doThrow(new BizException(SubjectErrorCode.SUBJECT_CODE_EXISTS))
                .when(subjectDomain).checkCodeExists(null, "saas");

        BizException exception = assertThrows(BizException.class,
                () -> subjectService.create(createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_CODE_EXISTS.getCode(), exception.getCode());
        verify(subjectMapper, never()).insert(any(Subject.class));
    }

    @Test
    void createShouldThrowBizExceptionWhenRealmNotExists() {
        when(subjectTypeDomain.findById(1L)).thenReturn(createSubjectType());
        when(subjectMapper.countRealmById(1L)).thenReturn(0L);

        BizException exception = assertThrows(BizException.class,
                () -> subjectService.create(createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_REALM_DATA_ERROR.getCode(), exception.getCode());
        verify(subjectMapper, never()).insert(any(Subject.class));
    }

    @Test
    void editShouldUpdateExistingSubject() {
        Subject subject = createSubject();
        when(subjectDomain.findById(1L)).thenReturn(subject);
        mockValidReference();
        SubjectRequest request = createRequest();
        request.setName("SaaS运营主体");

        Boolean result = subjectService.edit(1L, request);

        assertTrue(result);
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectMapper).updateById(captor.capture());
        assertSame(subject, captor.getValue());
        assertEquals("SaaS运营主体", captor.getValue().getName());
    }

    @Test
    void editShouldRejectParentSelfReference() {
        SubjectRequest request = createRequest();
        request.setParentSubjectId(1L);
        when(subjectDomain.findById(1L)).thenReturn(createSubject());
        when(subjectTypeDomain.findById(1L)).thenReturn(createSubjectType());
        when(subjectMapper.countRealmById(1L)).thenReturn(1L);

        BizException exception = assertThrows(BizException.class,
                () -> subjectService.edit(1L, request));

        assertEquals(SubjectErrorCode.SUBJECT_PARENT_SELF_DENIED.getCode(), exception.getCode());
        verify(subjectMapper, never()).updateById(any(Subject.class));
    }

    @Test
    void editStatusShouldToggleStatus() {
        Subject subject = createSubject();
        subject.setStatus(StatusEnum.NORMAL.getCode());
        when(subjectDomain.findById(1L)).thenReturn(subject);

        Boolean result = subjectService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void deleteShouldRejectBuiltInSubject() {
        Subject subject = createSubject();
        subject.setBuiltIn(1);
        when(subjectDomain.findById(1L)).thenReturn(subject);

        BizException exception = assertThrows(BizException.class,
                () -> subjectService.delete(1L));

        assertEquals(SubjectErrorCode.SUBJECT_BUILT_IN_DELETE_DENIED.getCode(), exception.getCode());
        verify(subjectMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteShouldRemoveCustomSubject() {
        Subject subject = createSubject();
        subject.setBuiltIn(0);
        when(subjectDomain.findById(1L)).thenReturn(subject);

        Boolean result = subjectService.delete(1L);

        assertTrue(result);
        verify(subjectMapper).deleteById(1L);
    }

    private void mockValidReference() {
        when(subjectTypeDomain.findById(1L)).thenReturn(createSubjectType());
        when(subjectMapper.countRealmById(1L)).thenReturn(1L);
    }

    private SubjectRequest createRequest() {
        SubjectRequest request = new SubjectRequest();
        request.setSubjectTypeId(1L);
        request.setRealmId(1L);
        request.setCode("saas");
        request.setName("SaaS运营方");
        request.setIsRoot(1);
        request.setBuiltIn(0);
        request.setDescription("测试主体");
        return request;
    }

    private Subject createSubject() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setSubjectTypeId(1L);
        subject.setRealmId(1L);
        subject.setCode("saas");
        subject.setName("SaaS运营方");
        subject.setIsRoot(1);
        subject.setBuiltIn(0);
        subject.setStatus(StatusEnum.NORMAL.getCode());
        return subject;
    }

    private SubjectType createSubjectType() {
        SubjectType subjectType = new SubjectType();
        subjectType.setId(1L);
        subjectType.setCode("saas_platform");
        subjectType.setName("SaaS平台");
        return subjectType;
    }
}
