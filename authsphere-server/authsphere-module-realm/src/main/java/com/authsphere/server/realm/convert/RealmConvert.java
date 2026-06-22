package com.authsphere.server.realm.convert;

import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.model.Realm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
@Mapper
public interface RealmConvert {
    RealmConvert INSTANCE = Mappers.getMapper(RealmConvert.class);


    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Realm model(CreateRealmRequest createRealmRequest);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void copyByModel(CreateRealmRequest createRealmRequest, @MappingTarget Realm realm);

    @Mapping(target = "typeCategoryId", source = "realmTypeId")
    @Mapping(target = "loginUrl", ignore = true)
    RealmInfoResponse toRealmInfoResponse(Realm byId);

    List<RealmInfoResponse> toRealmInfoResponse(List<Realm> realms);

    List<RealmListResponse> toRealmListResponse(List<Realm> realms);
}
