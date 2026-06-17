package com.authsphere.server.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.role.model.RoleResource;
import com.authsphere.server.role.service.RoleResourceService;
import com.authsphere.server.role.mapper.RoleResourceMapper;
import org.springframework.stereotype.Service;

/**
 * 角色资源关系基础 Service 实现。
 */
@Service
public class RoleResourceServiceImpl extends ServiceImpl<RoleResourceMapper, RoleResource>
    implements RoleResourceService{

}




