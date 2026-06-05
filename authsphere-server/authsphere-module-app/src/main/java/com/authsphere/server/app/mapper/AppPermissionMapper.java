package com.authsphere.server.app.mapper;

import com.authsphere.server.app.dto.AppClientPermissionResponse;
import com.authsphere.server.app.dto.AppPermissionPageRequest;
import com.authsphere.server.app.model.AppClientPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author longjiangran
* @description 针对表【app_permission(应用权限资源表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.AppPermission
*/
@Mapper
public interface AppPermissionMapper extends BaseMapper<AppClientPermission> {

    Page<AppClientPermissionResponse> page(@Param("page") Page<AppClientPermission> page, @Param("request") AppPermissionPageRequest request, @Param("appId") Long appId, @Param("clientId") Long clientId, @Param("permissionType") Integer permissionType);
}




