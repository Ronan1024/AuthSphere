package com.authsphere.account.service.impl;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPasswordResetRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.enums.AccountStatus;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.security.PasswordHash;
import com.authsphere.server.account.security.PasswordHashService;
import com.authsphere.server.account.service.impl.AccountServiceImpl;
import com.authsphere.server.api.realm.RealmApi;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Account 服务单元测试。
 *
 * @author L.J.Ran
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AccountServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private com.authsphere.server.account.domain.AccountDomain accountDomain;

    @Mock
    private com.authsphere.server.account.domain.AccountCredentialDomain accountCredentialDomain;

    @Mock
    private RealmApi realmApi;

    @Mock
    private com.authsphere.server.api.subject.SubjectMemberApi subjectMemberApi;

    @Mock
    private PasswordHashService passwordHashService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void pageShouldDelegateToMapperWithPageAndQueryCondition() {
        AccountPageRequest request = new AccountPageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setRealmCode("main");
        request.setUsername("admin");

        Page<AccountPageResponse> mapperResult = new Page<>(1, 10);
        when(accountMapper.page(any(Page.class), same(request))).thenReturn(mapperResult);

        Page<AccountPageResponse> result = accountService.page(request);

        assertSame(mapperResult, result);
        ArgumentCaptor<IPage<AccountPageResponse>> pageCaptor = ArgumentCaptor.forClass(IPage.class);
        verify(accountMapper).page(pageCaptor.capture(), same(request));
        assertEquals(1, pageCaptor.getValue().getCurrent());
        assertEquals(10, pageCaptor.getValue().getSize());
    }

    @Test
    void createShouldInsertAccountWhenUsernameNotExistsInRealm() {
        AccountCreateRequest request = createRequest();
        request.setStatus(null);
        when(realmApi.info(1L)).thenReturn(new RealmInfoResponse());
        when(passwordHashService.hash("AuthSphere#123"))
                .thenReturn(new PasswordHash("hashed-password", "salt-value", "PBKDF2_SHA256"));

        Boolean result = accountService.create(request);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).insert(captor.capture());
        Account account = captor.getValue();
        assertEquals(1L, account.getRealmId());
        assertEquals("admin", account.getUsername());
        assertEquals("hashed-password", account.getPassword());
        assertEquals("admin@example.com", account.getEmail());
        assertEquals("13800138000", account.getMobile());
        assertEquals(AccountStatus.ENABLED.getCode(), account.getStatus());
        verify(accountMapper).upsertPasswordCredential(account, "hashed-password", "salt-value", "PBKDF2_SHA256", Boolean.FALSE);
    }

    @Test
    void createShouldThrowBizExceptionWhenUsernameExistsInRealm() {
        AccountCreateRequest request = createRequest();
        when(realmApi.info(1L)).thenReturn(new RealmInfoResponse());
        Mockito.doThrow(new BizException(AccountErrorCode.ACCOUNT_USERNAME_EXISTS))
                .when(accountDomain).checkUsernameExists(null, request);

        BizException exception = assertThrows(BizException.class, () -> accountService.create(request));

        assertEquals(AccountErrorCode.ACCOUNT_USERNAME_EXISTS.getCode(), exception.getCode());
        assertEquals(AccountErrorCode.ACCOUNT_USERNAME_EXISTS.getMessage(), exception.getMessage());
        verify(accountMapper, never()).insert(any(Account.class));
    }

    @Test
    void updateShouldCopyRequestFieldsToExistingAccount() {
        Account existingAccount = new Account();
        existingAccount.setId(1L);
        existingAccount.setRealmId(1L);
        existingAccount.setUsername("old");
        when(accountDomain.findById(1L)).thenReturn(existingAccount);

        AccountCreateRequest request = createRequest();
        request.setUsername("new");
        request.setEmail("new@example.com");
        Boolean result = accountService.update(1L, request);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(captor.capture());
        Account account = captor.getValue();
        assertSame(existingAccount, account);
        assertEquals(1L, account.getId());
        assertEquals("new", account.getUsername());
        assertEquals("new@example.com", account.getEmail());
        assertEquals("13800138000", account.getMobile());
    }

    @Test
    void editStatusShouldDisableNormalAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(StatusEnum.NORMAL.getCode());
        when(accountDomain.findById(1L)).thenReturn(account);

        Boolean result = accountService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(captor.capture());
        assertEquals(AccountStatus.DISABLED.getCode(), captor.getValue().getStatus());
    }

    @Test
    void editStatusShouldEnableDisabledAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.DISABLED.getCode());
        when(accountDomain.findById(1L)).thenReturn(account);

        Boolean result = accountService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(captor.capture());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }

    @Test
    void resetPasswordShouldHashPasswordBeforePersisting() {
        Account account = new Account();
        account.setId(1L);
        account.setRealmId(1L);
        when(accountDomain.findById(1L)).thenReturn(account);
        when(passwordHashService.hash("Next#Password1"))
                .thenReturn(new PasswordHash("reset-hash", "reset-salt", "PBKDF2_SHA256"));

        AccountPasswordResetRequest request = new AccountPasswordResetRequest();
        request.setNewPassword("Next#Password1");
        request.setForceReset(Boolean.TRUE);

        Boolean result = accountService.resetPassword(1L, request);

        assertTrue(result);
        assertEquals("reset-hash", account.getPassword());
        verify(accountMapper).updateById(account);
        verify(accountMapper).upsertPasswordCredential(account, "reset-hash", "reset-salt", "PBKDF2_SHA256", Boolean.TRUE);
    }



    private AccountCreateRequest createRequest() {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setRealmId(1L);
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setMobile("13800138000");
        request.setPassword("AuthSphere#123");
        request.setStatus(AccountStatus.ENABLED.getCode());
        return request;
    }
}
