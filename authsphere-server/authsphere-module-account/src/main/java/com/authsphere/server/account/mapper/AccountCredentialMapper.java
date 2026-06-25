package com.authsphere.server.account.mapper;

import com.authsphere.server.account.model.AccountCredential;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author longjiangran
 * @description 针对表【account_credential(账号凭证表)】的数据库操作Mapper
 * @createDate 2026-06-25 15:33:53
 * @Entity com.authsphere.server.account.model.AccountCredential
 */
@Mapper
public interface AccountCredentialMapper extends BaseMapper<AccountCredential> {

}




