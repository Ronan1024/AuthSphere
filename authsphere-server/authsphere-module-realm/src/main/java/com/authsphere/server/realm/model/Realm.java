package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份域信息
 * @TableName realm
 */
@Data
@TableName(value ="realm")
@EqualsAndHashCode(callSuper = true)
public class Realm  extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 身份域编码， 唯一
     */
    private String code;

    /**
     * 身份域名称
     */
    private String name;

    /**
     * 身份域类型
     */
    private Long realmTypeId;

    /**
     * 默认认证策略ID。
     */
    private Long authPolicyId;

    /**
     * 是否允许注册
     */
    private Boolean registerEnabled;

    /**
     * 是否开启SSO
     */
    private Boolean ssoEnabled;

    /**
     * SSO 会话有效期，单位小时
     */
    private Integer ssoSessionTimeout;

    /**
     * SSO 空闲超时，单位分钟
     */
    private Integer ssoIdleTimeout;

    /**
     * 单点退出策略
     */
    private String ssoSingleLogout;

    /**
     * 已存在会话处理方式
     */
    private String existingSessionHandler;

    /**
     * 无 client_id 时的处理方式
     */
    private String noClientIdHandler;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 密码策略
     */
    private Long passwordPolicy;

    /**
     * MFA 策略
     */
    private Long mfaPolicy;

    /**
     * 账号唯一性规则
     */
    private Long uniquePolicy;

    /**
     * 描述
     */
    private String description;
}
