package com.authsphere.server.role.mapper;

import com.authsphere.server.role.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.role.dto.RolePageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author longjiangran
* @description 针对表【role(角色表)】的数据库操作Mapper
* @createDate 2026-06-06 00:49:42
* @Entity com.authsphere.server.role.model.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 分页查询角色列表。
     *
     * @param page    MyBatis-Plus 分页对象
     * @param request 查询条件
     * @return 角色分页结果
     */
    Page<Role> page(@Param("page") IPage<Role> page, @Param("request") RolePageRequest request);

}




