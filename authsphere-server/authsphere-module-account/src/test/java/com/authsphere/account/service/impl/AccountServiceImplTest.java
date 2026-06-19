package com.authsphere.account.service.impl;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountPageRequest;
import com.authsphere.server.account.dto.AccountPageResponse;
import com.authsphere.server.account.enums.AccountStatus;
import com.authsphere.server.account.error.AccountErrorCode;
import com.authsphere.server.account.mapper.AccountMapper;
import com.authsphere.server.account.model.Account;
import com.authsphere.server.account.service.impl.AccountServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private com.authsphere.server.api.RealmApi realmApi;

    @Mock
    private com.authsphere.server.api.subject.SubjectMemberApi subjectMemberApi;

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
        when(accountMapper.countEnabledRealmById(1L)).thenReturn(1L);
        when(accountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        Boolean result = accountService.create(request);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).insert(captor.capture());
        Account account = captor.getValue();
        assertEquals(1L, account.getRealmId());
        assertEquals("admin", account.getUsername());
        assertEquals("admin@example.com", account.getEmail());
        assertEquals("13800138000", account.getMobile());
        assertEquals(AccountStatus.ENABLED.getCode(), account.getStatus());
    }

    @Test
    void createShouldThrowBizExceptionWhenUsernameExistsInRealm() {
        AccountCreateRequest request = createRequest();
        when(accountMapper.countEnabledRealmById(1L)).thenReturn(1L);
        when(accountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new Account()));

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
        when(accountMapper.selectById(1L)).thenReturn(existingAccount);
        when(accountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

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
        when(accountMapper.selectById(1L)).thenReturn(account);

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
        when(accountMapper.selectById(1L)).thenReturn(account);

        Boolean result = accountService.editStatus(1L);

        assertTrue(result);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).updateById(captor.capture());
        assertEquals(StatusEnum.NORMAL.getCode(), captor.getValue().getStatus());
    }



    private AccountCreateRequest createRequest() {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setRealmId(1L);
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setMobile("13800138000");
        request.setStatus(AccountStatus.ENABLED.getCode());
        return request;
    }
}
