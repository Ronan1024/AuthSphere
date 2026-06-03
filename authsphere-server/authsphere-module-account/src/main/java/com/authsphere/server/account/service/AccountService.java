package com.authsphere.server.account.service;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountExternalIdentityResponse;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.dto.AccountLoginLogPageRequest;
import com.authsphere.server.account.dto.AccountLoginLogResponse;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.dto.AccountPasswordResetRequest;
import com.authsphere.server.account.dto.AccountSubjectResponse;
import com.authsphere.server.account.model.Account;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
* @author longjiangran
* @description 针对表【account(账号表)】的数据库操作Service
* @createDate 2026-05-28 16:01:48
*/
public interface AccountService extends IService<Account> {

    /**
     * 账号分页列表
     */
    Page<AccountPageResponse> page(AccountPageRequest request);

    /**
     * 账号详情。
     */
    AccountInfoResponse detail(Long id);

    /**
     * 新增账号
     */
    @Transactional(rollbackFor = Exception.class)
    Boolean create(AccountCreateRequest request);

    /**
     * 修改账号
     *
     * @param id 账号id
     */
    Boolean update(Long id, AccountCreateRequest request);

    /**
     * 修改账号状态
     *
     * @param id 账号id
     */
    Boolean editStatus(Long id);

    /**
     * 锁定账号。
     */
    Boolean lock(Long id);

    /**
     * 解锁账号。
     */
    Boolean unlock(Long id);

    /**
     * 重置密码。
     */
    Boolean resetPassword(Long id, AccountPasswordResetRequest request);

    /**
     * 查询账号加入的主体。
     */
    java.util.List<AccountSubjectResponse> listSubjects(Long id);

    /**
     * 查询账号第三方身份绑定。
     */
    java.util.List<AccountExternalIdentityResponse> listExternalIdentities(Long id);

    /**
     * 查询账号登录日志。
     */
    Page<AccountLoginLogResponse> pageLoginLogs(Long id, AccountLoginLogPageRequest request);
}
