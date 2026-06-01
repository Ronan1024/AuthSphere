package com.authsphere.server.subject.mapper;

import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.model.Subject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【subject(主体表)】的数据库操作Mapper
* @createDate 2026-06-01 14:19:02
* @Entity com.authsphere.server.subject.model.Subject
*/
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    Page<SubjectResponse> page(@Param("page") IPage<SubjectResponse> page,
                               @Param("request") SubjectPageRequest request);

    List<SubjectResponse> listAll();

    SubjectResponse detail(@Param("id") Long id);

    Long countRealmById(@Param("realmId") Long realmId);
}




