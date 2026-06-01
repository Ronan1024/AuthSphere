package com.authsphere.server.subject.mapper;

import com.authsphere.server.subject.dto.SubjectTypePageRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.model.SubjectType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【subject_type(主体类型表)】的数据库操作Mapper
 * @createDate 2026-06-01 13:36:39
 * @Entity com.authsphere.server.subject.model.SubjectType
 */
@Mapper
public interface SubjectTypeMapper extends BaseMapper<SubjectType> {

    Page<SubjectTypeResponse> page(@Param("page") IPage<SubjectTypeResponse> page,
                                   @Param("request") SubjectTypePageRequest request);


}




