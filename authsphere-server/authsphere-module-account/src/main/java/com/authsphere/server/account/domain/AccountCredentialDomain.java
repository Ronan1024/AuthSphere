package com.authsphere.server.account.domain;

import com.authsphere.server.account.mapper.AccountCredentialMapper;
import com.authsphere.server.account.model.AccountCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Service
@RequiredArgsConstructor
public class AccountCredentialDomain {
    private final AccountCredentialMapper accountCredentialMapper;


    /**
     * 保存账号凭证数据
     */
    public Boolean insert(AccountCredential accountCredential) {
        return accountCredentialMapper.insert(accountCredential) > 0;
    }



}
