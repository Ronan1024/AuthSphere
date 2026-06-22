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
     * 默认认证方式ID
     */
    private Long defaultAuthMethodId;

    /**
     * MFA认证方式ID
     */
    private Long mfaAuthMethodId;

    /**
     * 图形验证码模式
     */
    private String captchaMode;

    /**
     * 图形验证码阈值
     */
    private Integer captchaThreshold;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 密码最小长度
     */
    private Integer passwordMinLength;

    /**
     * 密码最大长度
     */
    private Integer passwordMaxLength;

    /**
     * 密码复杂度
     */
    private String passwordComplexity;

    /**
     * 密码过期天数
     */
    private Integer passwordExpireDays;

    /**
     * Access Token有效期（分钟）
     */
    private Integer accessTokenTimeout;

    /**
     * Refresh Token有效期（天）
     */
    private Integer refreshTokenTimeout;

    /**
     * 是否开启Refresh Token轮换
     */
    private Boolean tokenRotationEnabled;

    /**
     * 是否开启Token黑名单
     */
    private Boolean tokenBlacklistEnabled;

    /**
     * 会话空闲超时（分钟）
     */
    private Integer sessionIdleTimeout;

    /**
     * 多端会话策略
     */
    private String sessionMultiDevice;

    /**
     * 最大登录设备数
     */
    private Integer sessionMaxDevices;

    /**
     * 登录失败最大次数
     */
    private Integer loginFailMaxCount;

    /**
     * 登录失败统计窗口（分钟）
     */
    private Integer loginFailWindowMinutes;

    /**
     * 登录失败锁定时长（分钟）
     */
    private Integer loginFailLockMinutes;

    /**
     * 是否自动解锁
     */
    private Boolean loginFailAutoUnlock;

    /**
     * 描述
     */
    private String description;
}
