package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.CreateRealmTypeRequest;
import com.authsphere.server.realm.dto.RealmTypeInfoResponse;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.model.RealmType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 类型分类对象转换。
 */
@Mapper
public interface RealmTypeConvert {

    RealmTypeConvert INSTANCE = Mappers.getMapper(RealmTypeConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    RealmType model(CreateRealmTypeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(CreateRealmTypeRequest request, @MappingTarget RealmType realmType);

    RealmTypePageResponse response(RealmType typeCategory);

    @Mapping(target = "disabledCount", ignore = true)
    @Mapping(target = "realmList", ignore = true)
    RealmTypeInfoResponse infoResponse(RealmType realmType);

    List<RealmTypePageResponse> toTypeCategoryPageResponse(List<RealmType> allTypeCategories);

}
