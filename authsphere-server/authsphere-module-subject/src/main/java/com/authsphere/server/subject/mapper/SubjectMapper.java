package com.authsphere.server.subject.mapper;

import com.authsphere.server.subject.dto.SubjectDetailResponse;
import com.authsphere.server.subject.dto.SubjectPageRequest;
import com.authsphere.server.subject.dto.SubjectPageResponse;
import com.authsphere.server.subject.dto.SubjectRealmOption;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.model.Subject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author longjiangran
 * @description 针对表【subject(主体表)】的数据库操作Mapper
 * @createDate 2026-06-01 14:19:02
 * @Entity com.authsphere.server.subject.model.Subject
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 主体列表分页。
     */
    Page<SubjectPageResponse> page(@Param("page") IPage<SubjectPageResponse> page,
                                   @Param("request") SubjectPageRequest request);

    List<SubjectResponse> listAll();

    /**
     * 主体详情。
     */
    SubjectDetailResponse detail(@Param("id") Long id);


    /**
     * 批量查询身份域基础信息。
     */
    List<SubjectRealmOption> listRealmsByIds(@Param("ids") Set<Long> ids);

    /**
     * 查询指定父主体的直接子主体列表。
     */
    List<SubjectResponse> listChildren(@Param("parentId") Long parentId);
}



