package com.authsphere.server.subject.convert;

import com.authsphere.server.subject.dto.SubjectRequest;
import com.authsphere.server.subject.dto.SubjectResponse;
import com.authsphere.server.subject.model.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 主体对象转换。
 */
@Mapper
public interface SubjectConvert {

    SubjectConvert INSTANCE = Mappers.getMapper(SubjectConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Subject model(SubjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(SubjectRequest request, @MappingTarget Subject subject);

    @Mapping(target = "subjectTypeCode", ignore = true)
    @Mapping(target = "subjectTypeName", ignore = true)
    @Mapping(target = "realmCode", ignore = true)
    @Mapping(target = "realmName", ignore = true)
    @Mapping(target = "rootSubjectCode", ignore = true)
    @Mapping(target = "rootSubjectName", ignore = true)
    @Mapping(target = "parentSubjectCode", ignore = true)
    @Mapping(target = "parentSubjectName", ignore = true)
    SubjectResponse response(Subject subject);

    List<SubjectResponse> responseList(List<Subject> subjects);
}
