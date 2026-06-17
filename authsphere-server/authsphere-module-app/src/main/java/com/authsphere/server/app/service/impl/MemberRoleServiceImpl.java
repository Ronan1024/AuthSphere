package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.MemberRoleAssignRequest;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientInstanceMapper;
import com.authsphere.server.app.mapper.MemberRoleMapper;
//import com.authsphere.server.app.mapper.RoleMapper;
import com.authsphere.server.app.model.AppClientInstance;
import com.authsphere.server.app.model.MemberRole;
import com.authsphere.server.app.model.Role;
import com.authsphere.server.app.service.MemberRoleService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.enums.SubjectMemberStatus;
import com.authsphere.server.subject.mapper.SubjectMemberMapper;
import com.authsphere.server.subject.model.SubjectMember;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 成员角色授权服务实现。
 */
@Service
@RequiredArgsConstructor
public class MemberRoleServiceImpl extends ServiceImpl<MemberRoleMapper, MemberRole> implements MemberRoleService {

    private final MemberRoleMapper memberRoleMapper;
    private final AppClientInstanceMapper clientInstanceMapper;
//    private final RoleMapper roleMapper;
    private final SubjectMemberMapper subjectMemberMapper;

    @Override
    public List<MemberRole> listMemberRoles(Long clientInstanceId, Long memberId) {
        return memberRoleMapper.selectList(new LambdaQueryWrapper<MemberRole>()
                .eq(MemberRole::getClientInstanceId, clientInstanceId)
                .eq(MemberRole::getMemberId, memberId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignMemberRoles(Long clientInstanceId, Long memberId, MemberRoleAssignRequest request) {
        AppClientInstance clientInstance = ensureEnabledClientInstance(clientInstanceId);
        SubjectMember member = ensureEnabledMember(memberId, clientInstance.getOwnerSubjectId());
        memberRoleMapper.delete(new LambdaQueryWrapper<MemberRole>()
                .eq(MemberRole::getClientInstanceId, clientInstanceId)
                .eq(MemberRole::getMemberId, memberId));
        if (CollectionUtils.isEmpty(request.getRoleIds())) {
            return Boolean.TRUE;
        }
//        List<Role> roles = roleMapper.selectBatchIds(request.getRoleIds());
//        if (roles.size() != request.getRoleIds().size()) {
//            throw new BizException(AppErrorCode.MEMBER_ROLE_SCOPE_ERROR);
//        }
//        for (Role role : roles) {
//            if (!Objects.equals(role.getClientInstanceId(), clientInstanceId)
//                    || !Objects.equals(role.getStatus(), StatusEnum.NORMAL.getCode())) {
//                throw new BizException(AppErrorCode.MEMBER_ROLE_SCOPE_ERROR);
//            }
//            MemberRole memberRole = new MemberRole();
//            memberRole.setMemberId(member.getId());
//            memberRole.setRoleId(role.getId());
//            memberRole.setAppInstanceId(clientInstance.getAppInstanceId());
//            memberRole.setClientInstanceId(clientInstanceId);
//            memberRole.setGrantedByAccountId(request.getGrantedByAccountId());
//            memberRole.setGrantedAt(LocalDateTime.now());
//            memberRole.setStatus(StatusEnum.NORMAL.getCode());
//            memberRoleMapper.insert(memberRole);
//        }
        return Boolean.TRUE;
    }

    @Override
    public List<MemberRole> listByMember(Long memberId) {
        return memberRoleMapper.selectList(new LambdaQueryWrapper<MemberRole>()
                .eq(MemberRole::getMemberId, memberId));
    }

    private AppClientInstance ensureEnabledClientInstance(Long clientInstanceId) {
        AppClientInstance clientInstance = clientInstanceMapper.selectById(clientInstanceId);
        if (clientInstance == null || !Objects.equals(clientInstance.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_CLIENT_INSTANCE_DATA_ERROR);
        }
        return clientInstance;
    }

    private SubjectMember ensureEnabledMember(Long memberId, Long ownerSubjectId) {
        SubjectMember member = subjectMemberMapper.selectById(memberId);
        if (member == null
                || !Objects.equals(member.getStatus(), SubjectMemberStatus.ENABLED.getCode())
                || !Objects.equals(member.getSubjectId(), ownerSubjectId)) {
            throw new BizException(AppErrorCode.MEMBER_ROLE_MEMBER_ERROR);
        }
        return member;
    }
}
