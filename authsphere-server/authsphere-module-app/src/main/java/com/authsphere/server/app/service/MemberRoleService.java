package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.MemberRoleAssignRequest;
import com.authsphere.server.app.model.MemberRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 成员角色授权服务。
 */
public interface MemberRoleService extends IService<MemberRole> {

    List<MemberRole> listMemberRoles(Long clientInstanceId, Long memberId);

    Boolean assignMemberRoles(Long clientInstanceId, Long memberId, MemberRoleAssignRequest request);

    List<MemberRole> listByMember(Long memberId);
}
