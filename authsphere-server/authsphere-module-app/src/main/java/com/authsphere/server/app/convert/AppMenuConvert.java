package com.authsphere.server.app.convert;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.model.AppMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Mapper
public interface AppMenuConvert {

    AppMenuConvert INSTANCE = Mappers.getMapper(AppMenuConvert.class);

    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "children", ignore = true)
    AppMenuResponse toMenuResponse(AppMenu appMenu);

    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    AppMenu toAppMenu(AppMenuRequest request);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    void copyAppMenu(@MappingTarget AppMenu menu, AppMenuRequest request);
}
