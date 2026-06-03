package com.authsphere.server.account.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 账号凭证表
 * @TableName account_credential
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@TableName(value ="account_credential")
public class AccountCredential  extends BaseDataBaseModel {
    /**
     * 凭证ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号ID
     */
    private Long accountId;

    /**
     * 身份域ID，冗余便于查询
     */
    private Long realmId;

    /**
     * 凭证类型：1.PASSWORD 2.TOTP 3.PASSKEY
     */
    private Integer credentialType;

    /**
     * 凭证值，密码为hash
     */
    private String credentialValue;

    /**
     * 密码盐，可选
     */
    private String passwordSalt;

    /**
     * 密码算法，如 BCrypt
     */
    private String passwordAlgo;

    /**
     * 是否强制修改
     */
    private Boolean forceChange;

    /**
     * 凭证过期时间
     */
    private Date expireAt;

    /**
     * 状态 ENABLED/DISABLED
     */
    private Integer status;
}