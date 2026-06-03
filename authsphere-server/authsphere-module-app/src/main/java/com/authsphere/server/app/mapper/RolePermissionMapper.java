package com.authsphere.server.app.mapper;

import com.authsphere.server.app.model.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限 Mapper。
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
