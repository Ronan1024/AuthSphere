package com.authsphere.server.realm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 身份域详情响应。
 *
 * <p>列表页只返回摘要字段，详情页需要额外补齐认证与安全配置，
 * 因此单独定义该响应对象，避免把分页响应做成“全字段胖对象”。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmDetailResponse extends RealmPageResponse {

    /**
     * 当前身份域已绑定的认证方式 ID 列表。
     */
    private List<Long> authMethodIds;

    /**
     * 图形验证码模式。
     */
    private String captchaMode;

    /**
     * 图形验证码触发阈值，仅 threshold 模式有效。
     */
    private Integer captchaThreshold;

    /**
     * 密码最小长度。
     */
    private Integer passwordMinLength;

    /**
     * 密码最大长度。
     */
    private Integer passwordMaxLength;

    /**
     * 密码复杂度策略编码。
     */
    private String passwordComplexity;

    /**
     * 密码过期天数。
     */
    private Integer passwordExpireDays;

    /**
     * Access Token 有效期，单位分钟。
     */
    private Integer accessTokenTimeout;

    /**
     * Refresh Token 有效期，单位天。
     */
    private Integer refreshTokenTimeout;

    /**
     * 是否开启 Refresh Token 轮换。
     */
    private Boolean tokenRotationEnabled;

    /**
     * 是否开启 Token 黑名单。
     */
    private Boolean tokenBlacklistEnabled;

    /**
     * 会话空闲超时，单位分钟。
     */
    private Integer sessionIdleTimeout;

    /**
     * 多端会话策略。
     */
    private String sessionMultiDevice;

    /**
     * 最大登录设备数。
     */
    private Integer sessionMaxDevices;

    /**
     * 登录失败最大次数。
     */
    private Integer loginFailMaxCount;

    /**
     * 登录失败统计窗口，单位分钟。
     */
    private Integer loginFailWindowMinutes;

    /**
     * 登录失败锁定时长，单位分钟。
     */
    private Integer loginFailLockMinutes;

    /**
     * 是否允许自动解锁。
     */
    private Boolean loginFailAutoUnlock;
}
