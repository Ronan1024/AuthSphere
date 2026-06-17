package com.authsphere.server.subject.domain;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectMapper;
import com.authsphere.server.subject.model.Subject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * 主体领域服务。
 */
@Service
@RequiredArgsConstructor
public class SubjectDomain {

    private final SubjectMapper subjectMapper;

    /**
     * 根据 id 获取主体。
     */
    public Subject findById(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (ObjectUtils.isEmpty(subject)) {
            throw new BizException(SubjectErrorCode.SUBJECT_DATA_ERROR);
        }
        return subject;
    }

    /**
     * 校验主体编码唯一。
     */
    public void checkCodeExists(Long currentId, String code) {
        Long count = subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getCode, code)
                .ne(!ObjectUtils.isEmpty(currentId), Subject::getId, currentId)
        );
        if (count > 0) {
            throw new BizException(SubjectErrorCode.SUBJECT_CODE_EXISTS);
        }
    }
}
