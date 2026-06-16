package com.authsphere.server.role.mapper;

import com.authsphere.server.role.model.AccountRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账号角色关系 Mapper。
 *
 * <p>负责维护 account_role 表中账号、主体、客户端与角色的绑定关系。</p>
 */
@Mapper
public interface AccountRoleMapper extends BaseMapper<AccountRole> {
}
