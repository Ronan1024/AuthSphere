package com.authsphere.server.subject.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.domain.SubjectTypeRelationDomain;
import com.authsphere.server.subject.dto.SubjectTypeRelationPageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeRelationMapper;
import com.authsphere.server.subject.model.SubjectType;
import com.authsphere.server.subject.model.SubjectTypeRelation;
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
 * 主体类型关系规则服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubjectTypeRelationServiceImplTest {

    @Mock
    private SubjectTypeRelationMapper subjectTypeRelationMapper;

    @Mock
    private SubjectTypeDomain subjectTypeDomain;

    @Mock
    private SubjectTypeRelationDomain subjectTypeRelationDomain;

    @InjectMocks
    private SubjectTypeRelationServiceImpl relationService;

    @Test
    void pageShouldDelegateToMapperXml() {
        SubjectTypeRelationPageRequest request = new SubjectTypeRelationPageRequest();
        request.setPage(1);
        request.setSize(10);
        Page<SubjectTypeRelationResponse> mapperResult = new Page<>(1, 10);
        when(subjectTypeRelationMapper.page(any(Page.class), any(SubjectTypeRelationPageRequest.class)))
                .thenReturn(mapperResult);

        Page<SubjectTypeRelationResponse> result = relationService.page(request);

        assertSame(mapperResult, result);
    }

    @Test
    void listAllShouldDelegateToMapperXml() {
        SubjectTypeRelationResponse response = new SubjectTypeRelationResponse();
        response.setRelationType(1);
        when(subjectTypeRelationMapper.listAll()).thenReturn(List.of(response));

        List<SubjectTypeRelationResponse> result = relationService.listAll();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getRelationType());
    }

    @Test
    void createShouldInsertRelationWithNormalStatus() {
        mockSubjectTypes();
        when(subjectTypeRelationMapper.selectCount(any())).thenReturn(0L);

        Boolean result = relationService.create(createRequest());

        assertTrue(result);
        ArgumentCaptor<SubjectTypeRelation> captor = ArgumentCaptor.forClass(SubjectTypeRelation.class);
        verify(subjectTypeRelationMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getParentTypeId());
        assertEquals(2L, captor.getValue().getChildTypeId());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void createShouldRejectSelfRelation() {
        SubjectTypeRelationRequest request = createRequest();
        request.setChildTypeId(1L);

        BizException exception = assertThrows(BizException.class,
                () -> relationService.create(request));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_RELATION_SELF_DENIED.getCode(), exception.getCode());
        verify(subjectTypeRelationMapper, never()).insert(any(SubjectTypeRelation.class));
    }

    @Test
    void createShouldRejectDuplicateRelation() {
        mockSubjectTypes();
        when(subjectTypeRelationMapper.selectCount(any())).thenReturn(1L);

        BizException exception = assertThrows(BizException.class,
                () -> relationService.create(createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_RELATION_EXISTS.getCode(), exception.getCode());
        verify(subjectTypeRelationMapper, never()).insert(any(SubjectTypeRelation.class));
    }

    @Test
    void editShouldUpdateExistingRelation() {
        SubjectTypeRelation relation = createRelation();
        mockSubjectTypes();
        when(subjectTypeRelationDomain.findById(1L)).thenReturn(relation);
        when(subjectTypeRelationMapper.selectCount(any())).thenReturn(0L);

        SubjectTypeRelationRequest request = createRequest();
        request.setAllowManage(Boolean.FALSE);
        Boolean result = relationService.edit(1L, request);

        assertTrue(result);
        ArgumentCaptor<SubjectTypeRelation> captor = ArgumentCaptor.forClass(SubjectTypeRelation.class);
        verify(subjectTypeRelationMapper).updateById(captor.capture());
        assertSame(relation, captor.getValue());
        assertEquals(Boolean.FALSE, captor.getValue().getAllowManage());
    }

    @Test
    void editStatusShouldToggleStatus() {
        SubjectTypeRelation relation = createRelation();
        relation.setStatus(StatusEnum.NORMAL.getCode());
        when(subjectTypeRelationDomain.findById(1L)).thenReturn(relation);

        Boolean result = relationService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<SubjectTypeRelation> captor = ArgumentCaptor.forClass(SubjectTypeRelation.class);
        verify(subjectTypeRelationMapper).updateById(captor.capture());
        assertEquals(StatusEnum.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void deleteShouldRemoveExistingRelation() {
        when(subjectTypeRelationDomain.findById(1L)).thenReturn(createRelation());

        Boolean result = relationService.delete(1L);

        assertTrue(result);
        verify(subjectTypeRelationMapper).deleteById(1L);
    }

    @Test
    void editShouldThrowBizExceptionWhenRelationNotExists() {
        doThrow(new BizException(SubjectErrorCode.SUBJECT_TYPE_RELATION_DATA_ERROR))
                .when(subjectTypeRelationDomain).findById(1L);

        BizException exception = assertThrows(BizException.class,
                () -> relationService.edit(1L, createRequest()));

        assertEquals(SubjectErrorCode.SUBJECT_TYPE_RELATION_DATA_ERROR.getCode(), exception.getCode());
    }

    private void mockSubjectTypes() {
        when(subjectTypeDomain.findById(1L)).thenReturn(createSubjectType(1L));
        when(subjectTypeDomain.findById(2L)).thenReturn(createSubjectType(2L));
    }

    private SubjectTypeRelationRequest createRequest() {
        SubjectTypeRelationRequest request = new SubjectTypeRelationRequest();
        request.setParentTypeId(1L);
        request.setChildTypeId(2L);
        request.setRelationType(1);
        request.setAllowCreate(Boolean.TRUE);
        request.setAllowManage(Boolean.TRUE);
        request.setDescription("测试关系规则");
        return request;
    }

    private SubjectTypeRelation createRelation() {
        SubjectTypeRelation relation = new SubjectTypeRelation();
        relation.setId(1L);
        relation.setParentTypeId(1L);
        relation.setChildTypeId(2L);
        relation.setRelationType(1);
        relation.setAllowCreate(Boolean.TRUE);
        relation.setAllowManage(Boolean.TRUE);
        relation.setStatus(StatusEnum.NORMAL.getCode());
        return relation;
    }

    private SubjectType createSubjectType(Long id) {
        SubjectType subjectType = new SubjectType();
        subjectType.setId(id);
        subjectType.setCode("type-" + id);
        subjectType.setName("主体类型" + id);
        return subjectType;
    }
}
