package com.authsphere.server.account.service.impl;

import com.authsphere.server.account.convert.AccountConvert;
import com.authsphere.server.account.domain.AccountCredentialDomain;
import com.authsphere.server.account.domain.AccountDomain;
import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountExternalIdentityResponse;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.dto.AccountLoginLogPageRequest;
import com.authsphere.server.account.dto.AccountLoginLogResponse;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.dto.AccountPasswordResetRequest;
import com.authsphere.server.account.dto.AccountSubjectResponse;
import com.authsphere.server.account.enums.AccountStatus;
import com.authsphere.server.account.enums.CredentialType;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.model.AccountCredential;
import com.authsphere.server.account.service.AccountService;
import com.authsphere.server.api.RealmApi;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.api.model.dto.subject.AccountSubjectCountResponse;
import com.authsphere.server.api.subject.SubjectMemberApi;
import com.authsphere.server.common.enums.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author longjiangran
 * @description 针对表【account(账号表)】的数据库操作Service实现
 * @createDate 2026-05-28 16:01:48
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    private final AccountMapper accountMapper;

    private final AccountDomain accountDomain;

    private final AccountCredentialDomain accountCredentialDomain;

    private final RealmApi realmApi;

    private final SubjectMemberApi subjectMemberApi;


    /**
     * 账号分页列表
     */
    @Override
    public Page<AccountPageResponse> page(AccountPageRequest request) {
        Page<AccountPageResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<AccountPageResponse> result = accountMapper.page(page, request);
        List<AccountPageResponse> records = result.getRecords();

        if (!CollectionUtils.isEmpty(records)) {
            // 当前账号 加入的主体数量
            List<Long> accountId = records.stream().map(AccountPageResponse::getId).toList();
            List<AccountSubjectCountResponse> accountSubjectCount = subjectMemberApi.getAccountSubjectCount(accountId);
            Map<Long, Integer> accountSubjectCountMap = accountSubjectCount.stream().collect(Collectors.toMap(AccountSubjectCountResponse::getAccountId, AccountSubjectCountResponse::getCount));
            // 列表账号身份域信息
            List<Long> realmIdList = records.stream().map(AccountPageResponse::getRealmId).distinct().toList();
            List<RealmInfoResponse> realmInfoResponses = realmApi.list(realmIdList);
            Map<Long, RealmInfoResponse> realmInfoResponseMap = realmInfoResponses.stream().collect(Collectors.toMap(RealmInfoResponse::getId, e -> e));
            records.forEach(e -> {
                if (realmInfoResponseMap.containsKey(e.getRealmId())) {
                    RealmInfoResponse realmInfoResponse = realmInfoResponseMap.get(e.getRealmId());
                    e.setRealmName(realmInfoResponse.getName());
                    e.setRealmCode(realmInfoResponse.getCode());
                }
                e.setSubjectMemberCount(accountSubjectCountMap.get(e.getId()));
            });
        }
        return result;
    }

    /**
     * 获取账号详细信息
     */
    @Override
    public AccountInfoResponse detail(Long id) {
        Account account = accountDomain.findById(id);
        AccountInfoResponse result = AccountConvert.INSTANCE.toAccountInfoResponse(account);

        RealmInfoResponse info = realmApi.info(account.getRealmId());
        result.setRealmName(info.getName());

        List<AccountSubjectCountResponse> accountSubjectCount = subjectMemberApi.getAccountSubjectCount(Collections.singletonList(account.getId()));
        if (!CollectionUtils.isEmpty(accountSubjectCount)) {
            Map<Long, Integer> collect = accountSubjectCount.stream().collect(Collectors.toMap(AccountSubjectCountResponse::getAccountId, AccountSubjectCountResponse::getCount));
            result.setSubjectMemberCount(collect.getOrDefault(account.getId(), 0));
        }
        return result;
    }

    /**
     * 新增账号
     */
    @Override
    public Boolean create(AccountCreateRequest request) {
        realmApi.info(request.getRealmId());
        accountDomain.checkUsernameExists(null, request);

        Account account = AccountConvert.INSTANCE.toAccount(request);
        if (account.getStatus() == null) {
            account.setStatus(AccountStatus.ENABLED.getCode());
        }
        accountMapper.insert(account);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            // TODO 密码算法未处理
            AccountCredential accountCredential = AccountCredential.builder()
                    .accountId(account.getId())
                    .realmId(request.getRealmId())
                    .credentialType(CredentialType.PASSWORD.getCode())
                    .credentialValue(request.getPassword())
                    .passwordAlgo("xxx")
                    .forceChange(Boolean.FALSE)
                    .status(StatusEnum.NORMAL.getCode())
                    .build();
            accountCredentialDomain.insert(accountCredential);

        }
        return Boolean.TRUE;
    }

    /**
     * 修改账号
     *
     * @param id 账号id
     */
    @Override
    public Boolean update(Long id, AccountCreateRequest request) {
        Account account = accountDomain.findById(id);
        request.setRealmId(account.getRealmId());
        accountDomain.checkUsernameExists(id, request);

        AccountConvert.INSTANCE.copyToAccount(request, account);

        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    /**
     * 修改账号状态
     *
     * @param id 账号id
     */
    @Override
    public Boolean editStatus(Long id) {
        Account account = accountDomain.findById(id);
        Integer status = AccountStatus.isDisabled(account.getStatus())
                ? AccountStatus.ENABLED.getCode()
                : AccountStatus.DISABLED.getCode();
        account.setStatus(status);
        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    @Override
    public Boolean lock(Long id) {
        Account account = accountDomain.findById(id);
        if (AccountStatus.isDisabled(account.getStatus())) {
            throw new BizException(AccountErrorCode.ACCOUNT_STATUS_INVALID);
        }
        account.setStatus(AccountStatus.LOCKED.getCode());
        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    @Override
    public Boolean unlock(Long id) {
        Account account = accountDomain.findById(id);
        if (!AccountStatus.isLocked(account.getStatus())) {
            throw new BizException(AccountErrorCode.ACCOUNT_STATUS_INVALID);
        }
        account.setStatus(AccountStatus.ENABLED.getCode());
        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(Long id, AccountPasswordResetRequest request) {
        Account account = accountDomain.findById(id);
        account.setPassword(request.getNewPassword());
        accountMapper.updateById(account);
        accountMapper.upsertPasswordCredential(account, request.getNewPassword(), request.getForceReset());
        return Boolean.TRUE;
    }

    @Override
    public List<AccountSubjectResponse> listSubjects(Long id) {
        accountDomain.findById(id);
        return accountMapper.listSubjects(id);
    }

    @Override
    public List<AccountExternalIdentityResponse> listExternalIdentities(Long id) {
        accountDomain.findById(id);
        return accountMapper.listExternalIdentities(id);
    }

    @Override
    public Page<AccountLoginLogResponse> pageLoginLogs(Long id, AccountLoginLogPageRequest request) {
        accountDomain.findById(id);
        Page<AccountLoginLogResponse> page = new Page<>(request.getPage(), request.getSize());
        return accountMapper.pageLoginLogs(page, id, request);
    }
}


