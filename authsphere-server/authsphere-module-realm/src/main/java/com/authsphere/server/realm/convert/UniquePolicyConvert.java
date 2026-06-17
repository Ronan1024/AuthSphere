package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.UniquePolicyRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.model.UniquePolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 账号唯一性规则模板对象转换。
 */
@Mapper
public interface UniquePolicyConvert {

    UniquePolicyConvert INSTANCE = Mappers.getMapper(UniquePolicyConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UniquePolicy model(UniquePolicyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(UniquePolicyRequest request, @MappingTarget UniquePolicy policy);

    UniquePolicyResponse response(UniquePolicy policy);
}
