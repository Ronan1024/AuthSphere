package com.authsphere.server.subject.convert;

import com.authsphere.server.subject.dto.SubjectTypeRelationRequest;
import com.authsphere.server.subject.dto.SubjectTypeRelationResponse;
import com.authsphere.server.subject.model.SubjectTypeRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 主体类型关系规则对象转换。
 */
@Mapper
public interface SubjectTypeRelationConvert {

    SubjectTypeRelationConvert INSTANCE = Mappers.getMapper(SubjectTypeRelationConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SubjectTypeRelation model(SubjectTypeRelationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(SubjectTypeRelationRequest request, @MappingTarget SubjectTypeRelation relation);

    @Mapping(target = "parentTypeCode", ignore = true)
    @Mapping(target = "parentTypeName", ignore = true)
    @Mapping(target = "childTypeCode", ignore = true)
    @Mapping(target = "childTypeName", ignore = true)
    SubjectTypeRelationResponse response(SubjectTypeRelation relation);

    List<SubjectTypeRelationResponse> responseList(List<SubjectTypeRelation> relations);

    List<SubjectTypeRelationResponse> toSubjectTypeRelationResponse(List<SubjectTypeRelation> subjectTypeRelationList);

}
