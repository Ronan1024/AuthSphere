package com.authsphere.server.subject.service.impl;

import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.security.PasswordHash;
import com.authsphere.server.account.security.PasswordHashService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.domain.SubjectDomain;
import com.authsphere.server.subject.dto.SubjectMemberCreateAccountRequest;
import com.authsphere.server.subject.dto.SubjectMemberPageRequest;
import com.authsphere.server.subject.dto.SubjectMemberRequest;
import com.authsphere.server.subject.dto.SubjectMemberResponse;
import com.authsphere.server.subject.enums.SubjectMemberStatus;
import com.authsphere.server.subject.enums.SubjectMemberType;
import com.authsphere.server.subject.error.SubjectErrorCode;
import com.authsphere.server.subject.mapper.SubjectMemberMapper;
import com.authsphere.server.subject.mapper.SubjectTypeMapper;
import com.authsphere.server.subject.model.Subject;
import com.authsphere.server.subject.model.SubjectMember;
import com.authsphere.server.subject.model.SubjectType;
import com.authsphere.server.subject.service.SubjectMemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 主体成员服务实现。
 */
@Service
@RequiredArgsConstructor
public class SubjectMemberServiceImpl extends ServiceImpl<SubjectMemberMapper, SubjectMember> implements SubjectMemberService {

    private static final Integer ACCOUNT_LOCKED_STATUS = 3;

    private final SubjectMemberMapper subjectMemberMapper;
    private final SubjectDomain subjectDomain;
    private final SubjectTypeMapper subjectTypeMapper;
    private final AccountMapper accountMapper;
    private final PasswordHashService passwordHashService;

    @Override
    public Page<SubjectMemberResponse> page(SubjectMemberPageRequest request) {
        Page<SubjectMemberResponse> page = new Page<>(request.getPage(), request.getSize());
        return subjectMemberMapper.page(page, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addExisting(Long subjectId, SubjectMemberRequest request) {
        Subject subject = validateSubjectCanHaveMembers(subjectId);
        validateMemberType(request.getMemberType());
        Account account = validateUsableAccount(request.getAccountId());
        validateSameRealm(subject, account);
        checkActiveMemberExists(subjectId, request.getAccountId());
        createMember(subjectId, request.getAccountId(), request.getMemberType(), request.getDisplayName(), request.getRemark());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAccountAndAdd(Long subjectId, SubjectMemberCreateAccountRequest request) {
        Subject subject = validateSubjectCanHaveMembers(subjectId);
        validateMemberType(request.getMemberType());
        checkUsernameExists(subject.getRealmId(), request.getUsername());

        Account account = new Account();
        account.setRealmId(subject.getRealmId());
        account.setUsername(request.getUsername());
        account.setMobile(request.getMobile());
        account.setEmail(request.getEmail());
        PasswordHash passwordHash = passwordHashService.hash(request.getPassword());
        account.setPassword(passwordHash.hash());
        account.setStatus(StatusEnum.NORMAL.getCode());
        accountMapper.insert(account);
        accountMapper.upsertPasswordCredential(account, passwordHash.hash(), passwordHash.salt(), passwordHash.algorithm(), Boolean.FALSE);

        String displayName = StringUtils.hasText(request.getDisplayName()) ? request.getDisplayName() : request.getUsername();
        createMember(subjectId, account.getId(), request.getMemberType(), displayName, request.getRemark());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(Long id, SubjectMemberRequest request) {
        SubjectMember member = findActiveOrDisabledById(id);
        validateMemberType(request.getMemberType());
        if (isChangingLastOwner(member, request.getMemberType())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_LAST_OWNER_DENIED);
        }
        member.setMemberType(request.getMemberType());
        member.setDisplayName(request.getDisplayName());
        member.setRemark(request.getRemark());
        subjectMemberMapper.updateById(member);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enable(Long id) {
        SubjectMember member = findById(id);
        if (!Objects.equals(member.getStatus(), SubjectMemberStatus.DISABLED.getCode())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_STATUS_INVALID);
        }
        validateSubjectCanHaveMembers(member.getSubjectId());
        validateUsableAccount(member.getAccountId());
        member.setStatus(SubjectMemberStatus.ENABLED.getCode());
        member.setDisabledAt(null);
        member.setDisabledByAccountId(null);
        subjectMemberMapper.updateById(member);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id) {
        SubjectMember member = findById(id);
        if (!Objects.equals(member.getStatus(), SubjectMemberStatus.ENABLED.getCode())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_STATUS_INVALID);
        }
        if (isLastOwner(member)) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_LAST_OWNER_DENIED);
        }
        member.setStatus(SubjectMemberStatus.DISABLED.getCode());
        member.setDisabledAt(LocalDateTime.now());
        subjectMemberMapper.updateById(member);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean remove(Long id) {
        SubjectMember member = findActiveOrDisabledById(id);
        if (isLastOwner(member)) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_LAST_OWNER_DENIED);
        }
        member.setStatus(SubjectMemberStatus.REMOVED.getCode());
        member.setRemovedAt(LocalDateTime.now());
        subjectMemberMapper.updateById(member);
        return Boolean.TRUE;
    }

    private Subject validateSubjectCanHaveMembers(Long subjectId) {
        Subject subject = subjectDomain.findById(subjectId);
        if (!Objects.equals(subject.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(SubjectErrorCode.SUBJECT_DATA_ERROR);
        }
        SubjectType subjectType = subjectTypeMapper.selectById(subject.getSubjectTypeId());
        if (subjectType == null) {
            throw new BizException(SubjectErrorCode.SUBJECT_TYPE_DATA_ERROR);
        }
        if (!Boolean.TRUE.equals(subjectType.getCanHaveMembers())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_NOT_ALLOWED);
        }
        return subject;
    }

    private Account validateUsableAccount(Long accountId) {
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BizException(AccountErrorCode.ACCOUNT_DATA_ERROR);
        }
        if (!Objects.equals(account.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_ACCOUNT_STATUS_DENIED);
        }
        return account;
    }

    private void validateSameRealm(Subject subject, Account account) {
        if (!Objects.equals(subject.getRealmId(), account.getRealmId())) {
            throw new BizException(SubjectErrorCode.SUBJECT_REALM_DATA_ERROR);
        }
    }

    private void validateMemberType(Integer memberType) {
        if (!SubjectMemberType.valid(memberType)) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_TYPE_INVALID);
        }
    }

    private void checkActiveMemberExists(Long subjectId, Long accountId) {
        Long count = subjectMemberMapper.selectCount(new LambdaQueryWrapper<SubjectMember>()
                .eq(SubjectMember::getSubjectId, subjectId)
                .eq(SubjectMember::getAccountId, accountId)
                .ne(SubjectMember::getStatus, SubjectMemberStatus.REMOVED.getCode()));
        if (count > 0) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_EXISTS);
        }
    }

    private void checkUsernameExists(Long realmId, String username) {
        Long count = accountMapper.selectCount(new LambdaQueryWrapper<Account>()
                .eq(Account::getRealmId, realmId)
                .eq(Account::getUsername, username));
        if (count > 0) {
            throw new BizException(AccountErrorCode.ACCOUNT_USERNAME_EXISTS);
        }
    }

    private SubjectMember findById(Long id) {
        SubjectMember member = subjectMemberMapper.selectById(id);
        if (member == null) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_DATA_ERROR);
        }
        return member;
    }

    private SubjectMember findActiveOrDisabledById(Long id) {
        SubjectMember member = findById(id);
        if (SubjectMemberStatus.isRemoved(member.getStatus())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_STATUS_INVALID);
        }
        return member;
    }

    private void createMember(Long subjectId, Long accountId, Integer memberType, String displayName, String remark) {
        SubjectMember removedMember = subjectMemberMapper.selectOne(new LambdaQueryWrapper<SubjectMember>()
                .eq(SubjectMember::getSubjectId, subjectId)
                .eq(SubjectMember::getAccountId, accountId)
                .eq(SubjectMember::getStatus, SubjectMemberStatus.REMOVED.getCode()));
        if (removedMember != null) {
            removedMember.setMemberType(memberType);
            removedMember.setDisplayName(displayName);
            removedMember.setRemark(remark);
            removedMember.setStatus(SubjectMemberStatus.ENABLED.getCode());
            removedMember.setJoinedAt(LocalDateTime.now());
            removedMember.setDisabledAt(null);
            removedMember.setDisabledByAccountId(null);
            removedMember.setRemovedAt(null);
            removedMember.setRemovedByAccountId(null);
            subjectMemberMapper.updateById(removedMember);
            return;
        }
        SubjectMember member = new SubjectMember();
        member.setSubjectId(subjectId);
        member.setAccountId(accountId);
        member.setMemberType(memberType);
        member.setDisplayName(displayName);
        member.setRemark(remark);
        member.setStatus(SubjectMemberStatus.ENABLED.getCode());
        member.setJoinedAt(LocalDateTime.now());
        subjectMemberMapper.insert(member);
    }

    private boolean isChangingLastOwner(SubjectMember member, Integer newMemberType) {
        return Objects.equals(member.getMemberType(), SubjectMemberType.OWNER.getCode())
                && !Objects.equals(newMemberType, SubjectMemberType.OWNER.getCode())
                && countEnabledOwners(member.getSubjectId()) <= 1;
    }

    private boolean isLastOwner(SubjectMember member) {
        return Objects.equals(member.getMemberType(), SubjectMemberType.OWNER.getCode())
                && Objects.equals(member.getStatus(), SubjectMemberStatus.ENABLED.getCode())
                && countEnabledOwners(member.getSubjectId()) <= 1;
    }

    private long countEnabledOwners(Long subjectId) {
        return subjectMemberMapper.selectCount(new LambdaQueryWrapper<SubjectMember>()
                .eq(SubjectMember::getSubjectId, subjectId)
                .eq(SubjectMember::getMemberType, SubjectMemberType.OWNER.getCode())
                .eq(SubjectMember::getStatus, SubjectMemberStatus.ENABLED.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefault(Long id) {
        // 1. 校验成员记录存在且未被移除
        SubjectMember member = findActiveOrDisabledById(id);
        // 2. 只有启用状态的成员才能设为默认
        if (!Objects.equals(member.getStatus(), SubjectMemberStatus.ENABLED.getCode())) {
            throw new BizException(SubjectErrorCode.SUBJECT_MEMBER_STATUS_INVALID);
        }
        // 3. 幂等：已经是默认则直接返回
        if (Objects.equals(member.getIsDefault(), 1)) {
            return Boolean.TRUE;
        }
        // 4. 原子性：先清除该账号所有默认标记，再设置新默认
        subjectMemberMapper.clearDefault(member.getAccountId());
        subjectMemberMapper.setDefault(id);
        return Boolean.TRUE;
    }
}
