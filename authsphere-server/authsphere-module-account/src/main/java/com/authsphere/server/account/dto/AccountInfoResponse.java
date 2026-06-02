package com.authsphere.server.account.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class AccountInfoResponse extends BaseDataBaseModel {
    /**
     *
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long realmId;

    private String realmCode;

    private String realmName;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像。
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 账号状态：1.启用 2.锁定 3.禁用
     */
    private Integer status;

    private Long subjectMemberCount;

    private Long externalIdentityCount;

    private java.time.LocalDateTime lastLoginTime;
}
