package com.authsphere.server.subject.service;

import com.authsphere.server.subject.dto.SubjectMemberCreateAccountRequest;
import com.authsphere.server.subject.dto.SubjectMemberPageRequest;
import com.authsphere.server.subject.dto.SubjectMemberRequest;
import com.authsphere.server.subject.dto.SubjectMemberResponse;
import com.authsphere.server.subject.model.SubjectMember;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 主体成员服务。
 */
public interface SubjectMemberService extends IService<SubjectMember> {

    Page<SubjectMemberResponse> page(SubjectMemberPageRequest request);

    Boolean addExisting(Long subjectId, SubjectMemberRequest request);

    Boolean createAccountAndAdd(Long subjectId, SubjectMemberCreateAccountRequest request);

    Boolean edit(Long id, SubjectMemberRequest request);

    Boolean enable(Long id);

    Boolean disable(Long id);

    Boolean remove(Long id);

    /**
     * 将指定成员记录设为该账号的默认主体。
     * 同一账号在所有主体中最多只有一条 isDefault=1，操作是原子的。
     */
    Boolean setDefault(Long id);
}
