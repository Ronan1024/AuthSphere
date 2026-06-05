package com.authsphere.server.app.convert;

import com.authsphere.server.app.dto.AppInfoResponse;
import com.authsphere.server.app.dto.AppRequest;
import com.authsphere.server.app.model.App;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Mapper
public interface AppConvert {
    AppConvert INSTANCE = Mappers.getMapper(AppConvert.class);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    App toApp(AppRequest request);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    void copyApp(AppRequest request, @MappingTarget App app);

    @Mapping(target = "appInstanceSize", ignore = true)
    @Mapping(target = "clientSize", ignore = true)
    AppInfoResponse toAppInfoResponse(App byId);
}
