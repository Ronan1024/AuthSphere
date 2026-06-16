package com.authsphere.server.subject.domain;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeRelationMapper;
import com.authsphere.server.subject.model.SubjectTypeRelation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/1
 */
@Service
@RequiredArgsConstructor
public class SubjectTypeRelationDomain {
    private final SubjectTypeRelationMapper subjectTypeRelationMapper;


    /**
     * 查询主体分类型关系规则信息
     */
    public SubjectTypeRelation findById(Long id) {
        SubjectTypeRelation relation = subjectTypeRelationMapper.selectById(id);
        if (relation == null) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_RELATION_DATA_ERROR);
        }
        return relation;
    }
}
