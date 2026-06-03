package com.authsphere.server.app.mapper;

import com.authsphere.server.app.model.AppInstancePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author longjiangran
* @description 针对表【app_instance_permission(应用实例启用权限表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.AppInstancePermission
*/
@Mapper
public interface AppInstancePermissionMapper extends BaseMapper<AppInstancePermission> {

}




