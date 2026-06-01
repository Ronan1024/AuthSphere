package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.CreateTypeCategoryRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageResponse;
import com.authsphere.server.realm.model.TypeCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 类型分类对象转换。
 */
@Mapper
public interface TypeCategoryConvert {

    TypeCategoryConvert INSTANCE = Mappers.getMapper(TypeCategoryConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    TypeCategory model(CreateTypeCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(CreateTypeCategoryRequest request, @MappingTarget TypeCategory typeCategory);

    TypeCategoryPageResponse response(TypeCategory typeCategory);

    List<TypeCategoryPageResponse> toTypeCategoryPageResponse(List<TypeCategory> allTypeCategories);

}
