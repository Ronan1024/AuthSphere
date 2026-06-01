package com.authsphere.server.subject.mapper;

import com.authsphere.server.subject.dto.SubjectTypeRelationPageRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.model.SubjectTypeRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【subject_type_relation(主体类型关系规则表)】的数据库操作Mapper
 * @createDate 2026-06-01 14:15:22
 * @Entity com.authsphere.server.subject.model.SubjectTypeRelation
 */
@Mapper
public interface SubjectTypeRelationMapper extends BaseMapper<SubjectTypeRelation> {

    Page<SubjectTypeRelationResponse> page(@Param("page") IPage<SubjectTypeRelationResponse> page,
                                           @Param("request") SubjectTypeRelationPageRequest request);


}




