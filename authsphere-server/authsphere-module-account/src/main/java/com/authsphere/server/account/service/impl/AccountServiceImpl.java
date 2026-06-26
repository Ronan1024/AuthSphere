package com.authsphere.server.account.service.impl;

import com.authsphere.server.account.convert.AccountConvert;
import com.authsphere.server.account.domain.AccountDomain;
import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountCreateResponse;
import com.authsphere.server.account.dto.AccountExternalIdentityResponse;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.dto.AccountLoginLogPageRequest;
import com.authsphere.server.account.dto.AccountLoginLogResponse;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.dto.AccountPasswordResetRequest;
import com.authsphere.server.account.dto.AccountSubjectResponse;
import com.authsphere.server.account.enums.AccountStatus;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.model.AccountCredential;
import com.authsphere.server.account.security.PasswordHash;
import com.authsphere.server.account.security.PasswordHashService;
import com.authsphere.server.account.service.AccountService;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.api.realm.RealmApi;
import com.authsphere.server.api.model.dto.subject.AccountSubjectCountResponse;
import com.authsphere.server.api.subject.SubjectMemberApi;
import com.authsphere.server.common.enums.AuthMethodEnum;
import com.authsphere.server.common.enums.CredentialType;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.utils.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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

    private final RealmApi realmApi;

    private final SubjectMemberApi subjectMemberApi;

    private final PasswordHashService passwordHashService;


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
    public AccountCreateResponse create(AccountCreateRequest request) {
        RealmInfoResponse info = realmApi.info(request.getRealmId());
        accountDomain.checkUsernameExists(null, request);
        Account account = AccountConvert.INSTANCE.toAccount(request);
        if (account.getStatus() == null) {
            account.setStatus(AccountStatus.ENABLED.getCode());
        }
        AccountCredential accountCredential = null;
        String temporaryPassword = null;
        boolean useTemporaryPassword = Boolean.TRUE.equals(request.getUseTemporaryPassword());
        if (info.getAuthMethod().contains(AuthMethodEnum.PASSWORD_LOGIN.getCode())) {
            if (StringUtils.hasText(request.getPassword()) || useTemporaryPassword) {
                String rawPassword = useTemporaryPassword
                        ? passwordHashService.generateTemplatePassword(12)
                        : request.getPassword();
                PasswordHash hash = passwordHashService.hash(rawPassword);
                account.setPassword(hash.hash());
                accountCredential = new AccountCredential();
                accountCredential.setRealmId(info.getId());
                accountCredential.setCredentialType(CredentialType.PASSWORD.getCode());
                accountCredential.setCredentialValue(hash.hash());
                accountCredential.setCredentialSecret(hash.salt());
                accountCredential.setPasswordSalt(hash.salt());
                accountCredential.setPasswordAlgo(hash.algorithm());
                accountCredential.setPepperKeyId(passwordHashService.getProperties().getPepper());
                accountCredential.setStatus(StatusEnum.NORMAL.getCode());
                accountCredential.setForceChange(Boolean.FALSE);
                if (useTemporaryPassword) {
                    // 临时密码需进行登录强制修改 临时密码默认有效期 1 小时
                    accountCredential.setForceChange(Boolean.TRUE);
                    accountCredential.setExpireAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
                    temporaryPassword = rawPassword;
                }
            } else {
                Assert.isFalse(StringUtils.hasText(request.getPassword()), BizException.supplier("密码不能为空"));
            }
        }
        accountDomain.saveAccount(account, accountCredential);
        AccountCreateResponse response = new AccountCreateResponse();
        response.setTemporaryPassword(temporaryPassword);
        return response;
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
        PasswordHash passwordHash = passwordHashService.hash(request.getNewPassword());
        account.setPassword(passwordHash.hash());
        accountMapper.updateById(account);
        accountMapper.upsertPasswordCredential(account, passwordHash.hash(), passwordHash.salt(), passwordHash.algorithm(), request.getForceReset());
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
