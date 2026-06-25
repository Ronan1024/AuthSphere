package com.authsphere.server.account.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 账号凭证表
 *
 * @TableName account_credential
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "account_credential")
public class AccountCredential extends BaseDataBaseModel {
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
     * 凭证值。PASSWORD时为密码Hash；TOTP时可为加密后的Secret；PASSKEY时可为CredentialId
     */
    private String credentialValue;

    /**
     * 凭证密文。TOTP Secret或其他敏感凭证加密后存储
     */
    private String credentialSecret;

    /**
     * 公钥凭证，如Passkey/WebAuthn公钥
     */
    private String publicKey;

    /**
     * 密码盐值，可选；BCrypt/Argon2id通常已内嵌在Hash中
     */
    private String passwordSalt;

    /**
     * 密码算法，如 BCrypt/Argon2id
     */
    private String passwordAlgo;

    /**
     * 密码算法版本
     */
    private String passwordVersion;

    /**
     * Pepper密钥版本ID，只保存ID，不保存密钥
     */
    private String pepperKeyId;

    /**
     * 是否强制修改/重新设置
     */
    private Boolean forceChange;

    /**
     * 凭证过期时间
     */
    private Date expireAt;

    /**
     * 凭证最近使用时间
     */
    private Date lastUsedAt;

    /**
     * 状态：1.ENABLED 2.DISABLED
     */
    private Integer status;
}