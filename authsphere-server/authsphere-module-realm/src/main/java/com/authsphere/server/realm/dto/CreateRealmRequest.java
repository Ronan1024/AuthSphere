package com.authsphere.server.realm.dto;

import com.authsphere.server.realm.enums.*;
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
    private Boolean ssoEnabled = Boolean.TRUE;

    /**
     * SSO 会话有效期，单位小时。
     */
    private Integer ssoSessionTimeout = 8;

    /**
     * SSO 空闲超时，单位分钟。
     */
    private Integer ssoIdleTimeout = 30;

    /**
     * 单点退出策略：enabled 统一退出同域客户端，current_only 仅退出当前客户端。
     */
    private String ssoSingleLogout = SsoSingleLogoutEnum.ENABLED.getCode();

    /**
     * 已存在会话处理方式。
     */
    private String existingSessionHandler = ExistingSessionHandlerEnum.AUTO_REDIRECT.getCode();

    /**
     * 无 client_id 时的处理方式。
     */
    private String noClientIdHandler = NoClientIdHandlerEnum.SHOW_APP_LIST.getCode();

    /**
     * 支持的认证方式ID列表
     */
    private List<Long> authMethodIds;

    /**
     * 默认认证方式ID
     */
    @NotNull(message = "默认认证方式不能为空")
    private Long defaultAuthMethodId;

    /**
     * MFA认证方式ID
     */
    private Long mfaAuthMethodId;

    /**
     * 图形验证码模式
     */
    private String captchaMode = CaptchaModeEnum.NONE.getCode();

    /**
     * 图形验证码触发阈值
     */
    private Integer captchaThreshold;

    /**
     * 密码最小长度
     */
    private Integer passwordMinLength = 8;

    /**
     * 密码最大长度
     */
    private Integer passwordMaxLength = 32;

    /**
     * 密码复杂度
     */
    private String passwordComplexity = PasswordComplexityEnum.LETTERS_DIGITS.getCode();

    /**
     * 密码过期天数
     */
    private Integer passwordExpireDays = 90;

    /**
     * Access Token有效期（分钟）
     */
    private Integer accessTokenTimeout = 120;

    /**
     * Refresh Token有效期（天）
     */
    private Integer refreshTokenTimeout = 7;

    /**
     * 是否开启Refresh Token轮换
     */
    private Boolean tokenRotationEnabled = Boolean.TRUE;

    /**
     * 是否开启Token黑名单
     */
    private Boolean tokenBlacklistEnabled = Boolean.TRUE;

    /**
     * 会话空闲超时（分钟）
     */
    private Integer sessionIdleTimeout = 30;

    /**
     * 多端会话策略
     */
    private String sessionMultiDevice  = SessionMultiDeviceEnum.ALLOW.getCode();

    /**
     * 最大登录设备数
     */
    private Integer sessionMaxDevices = 5;

    /**
     * 登录失败最大次数
     */
    private Integer loginFailMaxCount = 5;

    /**
     * 登录失败统计窗口（分钟）
     */
    private Integer loginFailWindowMinutes = 10;

    /**
     * 登录失败锁定时长（分钟）
     */
    private Integer loginFailLockMinutes = 30;

    /**
     * 是否自动解锁
     */
    private Boolean loginFailAutoUnlock = Boolean.TRUE;

    /**
     * 描述
     */
    private String description;
}
