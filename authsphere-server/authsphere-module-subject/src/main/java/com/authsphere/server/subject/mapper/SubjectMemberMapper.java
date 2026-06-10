package com.authsphere.server.subject.mapper;

import com.authsphere.server.api.model.dto.subject.AccountSubjectCountResponse;
import com.authsphere.server.subject.dto.SubjectMemberPageRequest;
import com.authsphere.server.subject.dto.SubjectMemberResponse;
import com.authsphere.server.subject.model.SubjectMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 主体成员 Mapper。
 */
@Mapper
public interface SubjectMemberMapper extends BaseMapper<SubjectMember> {

    Page<SubjectMemberResponse> page(@Param("page") IPage<SubjectMemberResponse> page,
                                     @Param("request") SubjectMemberPageRequest request);

    /**
     * 获取当前账号加入的主体数量
     */
    List<AccountSubjectCountResponse> accountSubjectMemberCount(@Param("accountId") List<Long> accountId);
}
