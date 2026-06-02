package com.authsphere.server.account.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountPageRequest extends PageRequest {

    /**
     * 身份域 ID。
     */
    private Long realmId;

    /**
     * 身份域编号。
     */
    private String realmCode;

    /**
     * 账号关键词：用户名 / 手机号 / 邮箱 / 昵称。
     */
    private String keyword;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号状态。
     */
    private Integer status;
}
