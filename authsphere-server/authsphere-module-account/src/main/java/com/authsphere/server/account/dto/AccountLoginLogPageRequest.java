package com.authsphere.server.account.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号登录日志分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountLoginLogPageRequest extends PageRequest {

    private String loginType;

    private Boolean success;

    private String loginEntryCode;
}
