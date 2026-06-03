package com.authsphere.server.account.api;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountExternalIdentityResponse;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.dto.AccountLoginLogPageRequest;
import com.authsphere.server.account.dto.AccountLoginLogResponse;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.dto.AccountPasswordResetRequest;
import com.authsphere.server.account.dto.AccountSubjectResponse;
import com.authsphere.server.account.service.AccountService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账号管理
 *
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/account")
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取账号分页列表
     */
    @PostMapping("/page")
    public Page<AccountPageResponse> page(@RequestBody @Validated AccountPageRequest request) {
        return accountService.page(request);
    }

    /**
     * 账号详情。
     */
    @GetMapping("/{id}")
    public AccountInfoResponse detail(@PathVariable Long id) {
        return accountService.detail(id);
    }

    /**
     * 新增账号
     */
    @PostMapping
    public Boolean create(@RequestBody @Validated AccountCreateRequest request) {
        return accountService.create(request);
    }

    /**
     * 修改账号
     *
     * @param id 账号id
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable("id") Long id, @RequestBody @Validated AccountCreateRequest request) {
        return accountService.update(id, request);
    }

    /**
     * 启用/禁用账号
     *
     * @param id 账号id
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable("id") Long id) {
        return accountService.editStatus(id);
    }

    /**
     * 锁定账号。
     */
    @PutMapping("/{id}/lock")
    public Boolean lock(@PathVariable("id") Long id) {
        return accountService.lock(id);
    }

    /**
     * 解锁账号。
     */
    @PutMapping("/{id}/unlock")
    public Boolean unlock(@PathVariable("id") Long id) {
        return accountService.unlock(id);
    }

    /**
     * 重置账号密码。
     */
    @PutMapping("/{id}/password")
    public Boolean resetPassword(@PathVariable("id") Long id,
                                 @RequestBody @Validated AccountPasswordResetRequest request) {
        return accountService.resetPassword(id, request);
    }

    /**
     * 查询账号加入的主体。
     */
    @GetMapping("/{id}/subjects")
    public List<AccountSubjectResponse> listSubjects(@PathVariable("id") Long id) {
        return accountService.listSubjects(id);
    }

    /**
     * 查询账号第三方身份绑定。
     */
    @GetMapping("/{id}/external-identities")
    public List<AccountExternalIdentityResponse> listExternalIdentities(@PathVariable("id") Long id) {
        return accountService.listExternalIdentities(id);
    }

    /**
     * 查询账号登录日志。
     */
    @PostMapping("/{id}/login-logs")
    public Page<AccountLoginLogResponse> pageLoginLogs(@PathVariable("id") Long id,
                                                       @RequestBody @Validated AccountLoginLogPageRequest request) {
        return accountService.pageLoginLogs(id, request);
    }


}
