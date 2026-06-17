package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.authsphere.server.realm.model.PasswordPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 密码策略模板对象转换。
 */
@Mapper
public interface PasswordPolicyConvert {

    PasswordPolicyConvert INSTANCE = Mappers.getMapper(PasswordPolicyConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    PasswordPolicy model(PasswordPolicyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void copyToModel(PasswordPolicyRequest request, @MappingTarget PasswordPolicy passwordPolicy);

    PasswordPolicyResponse response(PasswordPolicy passwordPolicy);

    List<PasswordPolicyListResponse> toPasswordPolicyListResponse(List<PasswordPolicy> passwordPolicies);
}
