package com.authsphere.server.app.convert;

import com.authsphere.server.app.dto.AppClientRequest;
import com.authsphere.server.app.dto.AppClientResponse;
import com.authsphere.server.app.model.AppClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Mapper
public interface AppClientConvert {
    AppClientConvert INSTANCE = Mappers.getMapper(AppClientConvert.class);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "appCode", ignore = true)
    AppClient toAppClient(AppClientRequest appClientRequest);

    @Mapping(target = "realmName", ignore = true)
    AppClientResponse toAppClientResponse(AppClient appClient);
}
