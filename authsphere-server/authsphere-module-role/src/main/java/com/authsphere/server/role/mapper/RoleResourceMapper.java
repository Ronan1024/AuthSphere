package com.authsphere.server.role.mapper;

import com.authsphere.server.role.model.RoleResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author longjiangran
* @description 针对表【role_resource(角色资源关系表)】的数据库操作Mapper
* @createDate 2026-06-06 00:49:42
* @Entity com.authsphere.server.role.model.RoleResource
*/
@Mapper
public interface RoleResourceMapper extends BaseMapper<RoleResource> {

}




