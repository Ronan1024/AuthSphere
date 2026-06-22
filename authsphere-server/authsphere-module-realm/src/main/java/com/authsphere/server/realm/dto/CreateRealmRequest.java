package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
@Data
public class CreateRealmRequest {

    /**
     * 身份域编码， 唯一
     */
    @NotEmpty(message = "身份域编号不能为空")
    private String code;

    /**
     * 身份域名称
     */
    @NotEmpty(message = "身份域名城不能为空")
    private String name;

    /**
     * 身份域类型id
     */
    @JsonAlias("typeCategoryId")
    @NotNull(message = "身份域类型不能为空")
    private Long realmTypeId;

    /**
     * 身份域状态
     */
    private Integer status;

    /**
     * 是否允许注册
     */
    @NotNull(message = "是否允许注册不能为空")
    private Boolean registerEnabled;

    /**
     * 是否开启SSO
     */
    private Boolean ssoEnabled;

    /**
     * SSO 会话有效期，单位小时。
     */
    private Integer ssoSessionTimeout;

    /**
     * SSO 空闲超时，单位分钟。
     */
    private Integer ssoIdleTimeout;

    /**
     * 单点退出策略：enabled 统一退出同域客户端，current_only 仅退出当前客户端。
     */
    private String ssoSingleLogout;

    /**
     * 已存在会话处理方式。
     */
    private String existingSessionHandler;

    /**
     * 无 client_id 时的处理方式。
     */
    private String noClientIdHandler;

    /**
     * 支持的认证方式ID列表
     */
    private List<Long> authMethodIds;

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
     * 图形验证码触发阈值
     */
    private Integer captchaThreshold;

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
