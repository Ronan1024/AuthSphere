package com.authsphere.server.subject.convert;

import com.authsphere.server.subject.dto.SubjectTypeInfoResponse;
import com.authsphere.server.subject.dto.SubjectTypeRequest;
import com.authsphere.server.subject.dto.SubjectTypeResponse;
import com.authsphere.server.subject.model.SubjectType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 主体类型对象转换。
 */
@Mapper
public interface SubjectTypeConvert {

    SubjectTypeConvert INSTANCE = Mappers.getMapper(SubjectTypeConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SubjectType model(SubjectTypeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(SubjectTypeRequest request, @MappingTarget SubjectType subjectType);

    SubjectTypeResponse response(SubjectType subjectType);

    List<SubjectTypeResponse> responseList(List<SubjectType> subjectTypes);

    List<SubjectTypeResponse> toSubjectTypeResponse(List<SubjectType> subjectTypes);

    SubjectTypeInfoResponse toSubjectTypeInfoResponse(SubjectType subjectType);
}
