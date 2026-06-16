package com.authsphere.server.subject.domain;

import com.authsphere.server.api.model.dto.subject.AccountSubjectCountResponse;
import com.authsphere.server.api.subject.SubjectMemberApi;
import com.authsphere.server.subject.mapper.SubjectMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Service
@RequiredArgsConstructor
public class SubjectMemberDomain implements SubjectMemberApi {

    private final SubjectMemberMapper subjectMemberMapper;

    /**
     * 获取当前账号加入的主体数量
     *
     */
    @Override
    public List<AccountSubjectCountResponse> getAccountSubjectCount(List<Long> accountId) {
        if (CollectionUtils.isEmpty(accountId)) {
            return Collections.emptyList();
        }
        return subjectMemberMapper.accountSubjectMemberCount(accountId);
    }
}
