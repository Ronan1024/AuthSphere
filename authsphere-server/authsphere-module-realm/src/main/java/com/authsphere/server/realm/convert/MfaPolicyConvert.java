package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.MfaPolicyRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.model.MfaPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * MFA 策略模板对象转换。
 */
@Mapper
public interface MfaPolicyConvert {

    MfaPolicyConvert INSTANCE = Mappers.getMapper(MfaPolicyConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    MfaPolicy model(MfaPolicyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(MfaPolicyRequest request, @MappingTarget MfaPolicy policy);

    MfaPolicyResponse response(MfaPolicy policy);
}
