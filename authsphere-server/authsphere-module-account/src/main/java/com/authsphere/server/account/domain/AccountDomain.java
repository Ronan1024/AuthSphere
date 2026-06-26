package com.authsphere.server.account.domain;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountCredentialMapper;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.model.AccountCredential;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Service
@RequiredArgsConstructor
public class AccountDomain {

    private final AccountMapper accountMapper;
    private final AccountCredentialMapper accountCredentialMapper;


    /**
     * 检查当前 username 是否存在
     */
    public void checkUsernameExists(Long currentId, AccountCreateRequest request) {
        Account account = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getRealmId, request.getRealmId())
                .eq(Account::getUsername, request.getUsername())
                .or().eq(Account::getEmail, request.getEmail())
                .or().eq(Account::getMobile, request.getMobile())
                .ne(!ObjectUtils.isEmpty(currentId), Account::getId, currentId)
        );
        if (!ObjectUtils.isEmpty(account) && account.getUsername().equals(request.getUsername())) {
            throw new BizException(AccountErrorCode.ACCOUNT_USERNAME_EXISTS);
        }

        if (!ObjectUtils.isEmpty(account) && account.getEmail().equals(request.getEmail())) {
            throw new BizException(AccountErrorCode.ACCOUNT_EMAIL_EXISTS);
        }
        if (!ObjectUtils.isEmpty(account) && account.getMobile().equals(request.getMobile())) {
            throw new BizException(AccountErrorCode.ACCOUNT_MOBILE_EXISTS);
        }
    }

    /**
     * 获取账号信息
     */
    public Account findById(Long id) {
        Account account = accountMapper.selectById(id);
        if (ObjectUtils.isEmpty(account)) {
            throw new BizException(AccountErrorCode.ACCOUNT_DATA_ERROR);
        }
        return account;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAccount(Account account, AccountCredential accountCredential) {
        accountMapper.insert(account);
        if (!ObjectUtils.isEmpty(accountCredential)) {
            accountCredential.setAccountId(account.getId());
            accountCredentialMapper.insert(accountCredential);
        }
        return Boolean.TRUE;
    }
}
