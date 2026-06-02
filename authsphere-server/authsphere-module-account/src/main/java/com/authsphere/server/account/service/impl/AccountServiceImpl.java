package com.authsphere.server.account.service.impl;

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
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.service.AccountService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【account(账号表)】的数据库操作Service实现
 * @createDate 2026-05-28 16:01:48
*/
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    private final AccountMapper accountMapper;

    /**
     * 账号分页列表
     */
    @Override
    public Page<AccountPageResponse> page(AccountPageRequest request) {
        Page<AccountPageResponse> page = new Page<>(request.getPage(), request.getSize());
        return accountMapper.page(page, request);
    }

    @Override
    public AccountInfoResponse detail(Long id) {
        findById(id);
        return accountMapper.detail(id);
    }

    /**
     * 新增账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(AccountCreateRequest request) {
        checkRealmExists(request.getRealmId());
        checkUsernameExists(null, request);
        Account account = buildAccount(request);
        if (account.getStatus() == null) {
            account.setStatus(AccountStatus.ENABLED.getCode());
        }
        accountMapper.insert(account);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            accountMapper.upsertPasswordCredential(account, request.getPassword(), Boolean.TRUE);
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
        Account account = findById(id);
        request.setRealmId(account.getRealmId());
        checkUsernameExists(id, request);
        copyToAccount(request, account);
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
        Account account = findById(id);
        Integer status = AccountStatus.isDisabled(account.getStatus())
                ? AccountStatus.ENABLED.getCode()
                : AccountStatus.DISABLED.getCode();
        account.setStatus(status);
        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    @Override
    public Boolean lock(Long id) {
        Account account = findById(id);
        if (AccountStatus.isDisabled(account.getStatus())) {
            throw new BizException(AccountErrorCode.ACCOUNT_STATUS_INVALID);
        }
        account.setStatus(AccountStatus.LOCKED.getCode());
        accountMapper.updateById(account);
        return Boolean.TRUE;
    }

    @Override
    public Boolean unlock(Long id) {
        Account account = findById(id);
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
        Account account = findById(id);
        account.setPassword(request.getNewPassword());
        accountMapper.updateById(account);
        accountMapper.upsertPasswordCredential(account, request.getNewPassword(), request.getForceReset());
        return Boolean.TRUE;
    }

    @Override
    public List<AccountSubjectResponse> listSubjects(Long id) {
        findById(id);
        return accountMapper.listSubjects(id);
    }

    @Override
    public List<AccountExternalIdentityResponse> listExternalIdentities(Long id) {
        findById(id);
        return accountMapper.listExternalIdentities(id);
    }

    @Override
    public Page<AccountLoginLogResponse> pageLoginLogs(Long id, AccountLoginLogPageRequest request) {
        findById(id);
        Page<AccountLoginLogResponse> page = new Page<>(request.getPage(), request.getSize());
        return accountMapper.pageLoginLogs(page, id, request);
    }

    public Account findById(Long id) {
        Account account = accountMapper.selectById(id);
        if (account == null) {
            throw new BizException(AccountErrorCode.ACCOUNT_DATA_ERROR);
        }
        return account;
    }

    private void checkUsernameExists(Long currentId, AccountCreateRequest request) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getRealmId, request.getRealmId())
                .eq(Account::getUsername, request.getUsername());
        if (currentId != null) {
            wrapper.ne(Account::getId, currentId);
        }
        List<Account> accounts = accountMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(accounts)) {
            throw new BizException(AccountErrorCode.ACCOUNT_USERNAME_EXISTS);
        }
    }

    private Account buildAccount(AccountCreateRequest request) {
        Account account = new Account();
        copyToAccount(request, account);
        return account;
    }

    private void copyToAccount(AccountCreateRequest request, Account account) {
        if (request.getRealmId() != null && account.getId() == null) {
            account.setRealmId(request.getRealmId());
        }
        account.setUsername(request.getUsername());
        account.setNickname(request.getNickname());
        account.setAvatar(request.getAvatar());
        account.setEmail(request.getEmail());
        account.setMobile(request.getMobile());
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(request.getPassword());
        }
    }

    private void checkRealmExists(Long realmId) {
        Long count = accountMapper.countEnabledRealmById(realmId);
        if (count == null || count == 0) {
            throw new BizException(AccountErrorCode.ACCOUNT_REALM_DATA_ERROR);
        }
    }

}


