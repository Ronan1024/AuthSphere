package com.authsphere.server.subject.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.convert.SubjectTypeRelationConvert;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.domain.SubjectTypeRelationDomain;
import com.authsphere.server.subject.dto.SubjectTypeRelationPageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeRelationMapper;
import com.authsphere.server.subject.model.SubjectTypeRelation;
import com.authsphere.server.subject.service.SubjectTypeRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author longjiangran
 * @description 针对表【subject_type_relation(主体类型关系规则表)】的数据库操作Service实现
 * @createDate 2026-06-01 14:15:22
 */
@Service
@RequiredArgsConstructor
public class SubjectTypeRelationServiceImpl extends ServiceImpl<SubjectTypeRelationMapper, SubjectTypeRelation>
        implements SubjectTypeRelationService {

    private final SubjectTypeRelationMapper subjectTypeRelationMapper;

    private final SubjectTypeRelationDomain subjectTypeRelationDomain;

    private final SubjectTypeDomain subjectTypeDomain;

    /**
     * 主体关系分页列表
     */
    @Override
    public Page<SubjectTypeRelationResponse> page(SubjectTypeRelationPageRequest request) {
        Page<SubjectTypeRelationResponse> page = new Page<>(request.getPage(), request.getSize());
        return subjectTypeRelationMapper.page(page, request);
    }

    /**
     * 主体类型关系全局列表
     */
    @Override
    public List<SubjectTypeRelationResponse> listAll() {
        List<SubjectTypeRelation> subjectTypeRelationList = subjectTypeRelationMapper.selectList(new LambdaQueryWrapper<SubjectTypeRelation>()
                .eq(SubjectTypeRelation::getStatus, StatusEnum.NORMAL.getCode()));
        return SubjectTypeRelationConvert.INSTANCE.toSubjectTypeRelationResponse(subjectTypeRelationList);

    }

    /**
     * 创建主体类型关系
     */
    @Override
    public Boolean create(SubjectTypeRelationRequest request) {
        validateRelation(null, request);
        SubjectTypeRelation relation = SubjectTypeRelationConvert.INSTANCE.model(request);
        relation.setStatus(StatusEnum.NORMAL.getCode());
        subjectTypeRelationMapper.insert(relation);
        return Boolean.TRUE;
    }

    /**
     * 编辑主体类型关系
     */
    @Override
    public Boolean edit(Long id, SubjectTypeRelationRequest request) {
        SubjectTypeRelation relation = subjectTypeRelationDomain.findById(id);
        validateRelation(id, request);
        SubjectTypeRelationConvert.INSTANCE.copyToModel(request, relation);
        subjectTypeRelationMapper.updateById(relation);
        return Boolean.TRUE;
    }

    /**
     * 编辑主体类型关系状态
     */
    @Override
    public Boolean editStatus(Long id) {
        SubjectTypeRelation relation = subjectTypeRelationDomain.findById(id);
        relation.setStatus(StatusEnum.reverseStatus(relation.getStatus()));
        subjectTypeRelationMapper.updateById(relation);
        return Boolean.TRUE;
    }

    /**
     * 删除主体类型关系
     */
    @Override
    public Boolean delete(Long id) {
        subjectTypeRelationDomain.findById(id);
        subjectTypeRelationMapper.deleteById(id);
        return Boolean.TRUE;
    }


    private void validateRelation(Long currentId, SubjectTypeRelationRequest request) {
        if (Objects.equals(request.getParentTypeId(), request.getChildTypeId())) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_RELATION_SELF_DENIED);
        }
        subjectTypeDomain.findById(request.getParentTypeId());
        subjectTypeDomain.findById(request.getChildTypeId());
        checkRelationExists(currentId, request);
    }

    private void checkRelationExists(Long currentId, SubjectTypeRelationRequest request) {
        LambdaQueryWrapper<SubjectTypeRelation> wrapper = new LambdaQueryWrapper<SubjectTypeRelation>()
                .eq(SubjectTypeRelation::getParentTypeId, request.getParentTypeId())
                .eq(SubjectTypeRelation::getChildTypeId, request.getChildTypeId())
                .eq(SubjectTypeRelation::getRelationType, request.getRelationType());
        if (currentId != null) {
            wrapper.ne(SubjectTypeRelation::getId, currentId);
        }
        if (subjectTypeRelationMapper.selectCount(wrapper) > 0) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_RELATION_EXISTS);
        }
    }

}
