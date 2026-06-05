package com.authsphere.server.app.convert;

import com.authsphere.server.app.dto.AppClientPermissionResponse;
import com.authsphere.server.app.dto.AppPermissionRequest;
import com.authsphere.server.app.model.AppClientPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/5
 */
@Mapper
public interface AppClientPermissionConvert {
    AppClientPermissionConvert INSTANCE = Mappers.getMapper(AppClientPermissionConvert.class);

    List<AppClientPermissionResponse> toAppClientPermissionResponse(List<AppClientPermission> appClientPermissions);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    AppClientPermission toAppCLientPermission(AppPermissionRequest request);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    void copyAppClientPermission(@MappingTarget AppClientPermission permission, AppPermissionRequest request);
}
