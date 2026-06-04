package com.authsphere.server.api.subject;

import com.authsphere.server.api.model.dto.subject.AccountSubjectCountResponse;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
public interface SubjectMemberApi {

    /**
     * 获取当前账号加入的主体数量
     */
    List<AccountSubjectCountResponse> getAccountSubjectCount(List<Long> accountId);
}
