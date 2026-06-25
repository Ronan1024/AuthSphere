package com.authsphere.server.realm.convert;

import com.authsphere.server.realm.dto.AuthMethodInfoResponse;
import com.authsphere.server.realm.model.AuthMethod;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/25
 */
@Mapper
public interface AuthMethodConvert {
    AuthMethodConvert INSTANCE = Mappers.getMapper(AuthMethodConvert.class);

    AuthMethodInfoResponse toAuthMethodInfoResponse(AuthMethod aDefault);
}
