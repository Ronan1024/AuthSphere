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
}
