package com.authsphere.server.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.account.model.AccountCredential;
import com.authsphere.server.account.service.AccountCredentialService;
import com.authsphere.server.account.mapper.AccountCredentialMapper;
import org.springframework.stereotype.Service;

/**
* @author longjiangran
* @description 针对表【account_credential(账号凭证表)】的数据库操作Service实现
* @createDate 2026-06-25 15:33:53
*/
@Service
public class AccountCredentialServiceImpl extends ServiceImpl<AccountCredentialMapper, AccountCredential>
    implements AccountCredentialService{

}




