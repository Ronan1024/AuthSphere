package com.authsphere.server.account.mapper;

import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.dto.AccountExternalIdentityResponse;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.dto.AccountLoginLogPageRequest;
import com.authsphere.server.account.dto.AccountLoginLogResponse;
import com.authsphere.server.account.dto.AccountSubjectResponse;
import com.authsphere.server.account.model.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author longjiangran
 * @description 针对表【account(账号表)】的数据库操作Mapper
 * @createDate 2026-05-28 16:01:48
 * @Entity com.authsphere.server.realm.model.Account
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 获取账号分页列表
     */
    Page<AccountPageResponse> page(@Param("page") IPage<AccountPageResponse> page, @Param("request") AccountPageRequest request);

    Long countRealmById(@Param("realmId") Long realmId);

    Long countEnabledRealmById(@Param("realmId") Long realmId);

    void upsertPasswordCredential(@Param("account") Account account,
                                  @Param("passwordHash") String passwordHash,
                                  @Param("passwordSalt") String passwordSalt,
                                  @Param("passwordAlgo") String passwordAlgo,
                                  @Param("forceReset") Boolean forceReset);

    java.util.List<AccountSubjectResponse> listSubjects(@Param("accountId") Long accountId);

    java.util.List<AccountExternalIdentityResponse> listExternalIdentities(@Param("accountId") Long accountId);

    Page<AccountLoginLogResponse> pageLoginLogs(@Param("page") IPage<AccountLoginLogResponse> page,
                                                @Param("accountId") Long accountId,
                                                @Param("request") AccountLoginLogPageRequest request);
}


