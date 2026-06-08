package com.authsphere.server.realm.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 认证策略详情响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthPolicyInfoResponse extends AuthPolicyResponse {

    /**
     * 策略限定适用的身份域主键；为空表示不限定身份域。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicableRealmId;

    /**
     * 策略限定适用的身份域名称。
     */
    private String applicableRealmName;

    /**
     * 策略默认使用的登录方式编码。
     */
    private String defaultAuthMethod;

    /**
     * 触发图形验证码前允许的连续认证失败次数。
     */
    private Integer captchaFailureThreshold;

    /**
     * 图形验证码有效期，单位为秒。
     */
    private Integer captchaTtlSeconds;

    /**
     * 单个图形验证码允许的最大错误次数。
     */
    private Integer captchaErrorLimit;

    /**
     * 认证失败次数统计周期，单位为分钟。
     */
    private Integer failureWindowMinutes;

    /**
     * 账号被锁定时是否通知用户。
     */
    private Boolean notifyUser;

    /**
     * 是否记录认证风险日志。
     */
    private Boolean riskLogEnabled;

    /**
     * 触发多因素认证的场景编码集合。
     */
    private List<String> mfaTriggers;

    /**
     * 多因素认证可使用的认证方式编码集合。
     */
    private List<String> mfaMethods;

    /**
     * 是否允许用户记住已验证设备。
     */
    private Boolean rememberDeviceEnabled;

    /**
     * 已验证设备的免验证有效天数。
     */
    private Integer rememberDeviceDays;

    /**
     * 是否启用登录IP限制。
     */
    private Boolean ipRestrictionEnabled;

    /**
     * 是否校验登录设备。
     */
    private Boolean deviceCheckEnabled;

    /**
     * 是否检测异地登录。
     */
    private Boolean remoteLoginCheckEnabled;

    /**
     * 是否检测异常时间登录。
     */
    private Boolean abnormalTimeCheckEnabled;

    /**
     * 是否为系统内置策略；系统内置策略不允许删除。
     */
    private Boolean systemBuiltin;

    /**
     * 认证策略说明。
     */
    private String description;

    /**
     * 引用该策略的身份域数量。
     */
    private Integer realmReferenceCount;

    /**
     * 引用该策略的客户端数量。
     */
    private Integer clientReferenceCount;

    /**
     * 引用该策略的身份域和客户端明细。
     */
    private List<AuthPolicyReferenceResponse> references;
}
