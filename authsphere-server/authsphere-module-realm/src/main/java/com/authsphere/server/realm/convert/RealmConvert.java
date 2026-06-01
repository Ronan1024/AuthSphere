package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.model.Realm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

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
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Realm model(CreateRealmRequest createRealmRequest);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void copyByModel(CreateRealmRequest createRealmRequest, @MappingTarget Realm realm);
}
