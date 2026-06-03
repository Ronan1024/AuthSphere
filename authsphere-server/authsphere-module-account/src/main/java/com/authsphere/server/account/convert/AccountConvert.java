package com.authsphere.server.account.convert;

import com.authsphere.server.account.dto.AccountCreateRequest;
import com.authsphere.server.account.dto.AccountInfoResponse;
import com.authsphere.server.account.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Mapper
public interface AccountConvert {
    AccountConvert INSTANCE = Mappers.getMapper(AccountConvert.class);


    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Account toAccount(AccountCreateRequest request);

    void copyToAccount(@MappingTarget AccountCreateRequest request, Account account);

    @Mapping(target = "subjectMemberCount", ignore = true)
    @Mapping(target = "realmName", ignore = true)
    @Mapping(target = "realmCode", ignore = true)
    @Mapping(target = "externalIdentityCount", ignore = true)
    AccountInfoResponse toAccountInfoResponse(Account account);
}
