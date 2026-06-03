package com.authsphere.server.account.domain;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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


    /**
     * 检查当前 username 是否存在
     */
    public void checkUsernameExists(Long currentId, AccountCreateRequest request) {
        List<Account> accounts = accountMapper.selectList(new LambdaQueryWrapper<Account>()
                .eq(Account::getRealmId, request.getRealmId())
                .eq(Account::getUsername, request.getUsername())
                .ne(!ObjectUtils.isEmpty(currentId), Account::getId, currentId)
        );
        if (!CollectionUtils.isEmpty(accounts)) {
            throw new BizException(AccountErrorCode.ACCOUNT_USERNAME_EXISTS);
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
}
