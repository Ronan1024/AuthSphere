package com.authsphere.server.subject.domain;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectTypeMapper;
import com.authsphere.server.subject.model.SubjectType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/1
 */
@Service
@RequiredArgsConstructor
public class SubjectTypeDomain {
    private final SubjectTypeMapper subjectTypeMapper;


    /**
     * 根据 id 获取主体类型信息
     */
    public SubjectType findById(Long id) {
        SubjectType subjectType = subjectTypeMapper.selectById(id);
        if (ObjectUtils.isEmpty(subjectType)) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_DATA_ERROR);
        }
        return subjectType;
    }

    /**
     * 校验当前 code 是否存在
     *
     * @param currentId 主体类型 id 可以为空
     * @param code      主体类型 code
     */
    public void checkCodeExists(Long currentId, String code) {
        Long count = subjectTypeMapper.selectCount(new LambdaQueryWrapper<SubjectType>()
                .eq(SubjectType::getCode, code)
                .ne(!ObjectUtils.isEmpty(currentId), SubjectType::getId, currentId)
        );
        if (count > 0) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_CODE_EXISTS);
        }
    }

}
