package com.authsphere.server.account.dto;

import lombok.Data;

/**
 * 新增账号响应。
 */
@Data
public class AccountCreateResponse {

    /**
     * 临时密码。仅在使用临时密码时返回。
     */
    private String temporaryPassword;
}
