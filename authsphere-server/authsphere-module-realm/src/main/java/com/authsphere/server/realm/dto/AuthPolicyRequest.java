package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 认证策略保存请求。
 */
@Data
public class AuthPolicyRequest {

    /**
     * 认证策略编码，创建后作为策略的稳定业务标识。
     */
    @NotBlank(message = "认证策略编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{1,63}$", message = "认证策略编码需以字母开头，仅支持字母、数字、下划线和长横线")
    private String code;

    /**
     * 认证策略名称，用于管理端展示。
     */
    @NotBlank(message = "认证策略名称不能为空")
    @Size(max = 128, message = "认证策略名称不能超过128个字符")
    private String name;

    /**
     * 策略限定适用的身份域主键；为空表示不限定身份域。
     */
    private Long applicableRealmId;

    /**
     * 策略允许使用的登录方式编码集合。
     */
    @NotEmpty(message = "至少选择一种登录方式")
    private List<String> authMethods;

    /**
     * 默认登录方式编码，必须包含在允许的登录方式集合中。
     */
    @NotBlank(message = "默认登录方式不能为空")
    private String defaultAuthMethod;

    /**
     * 是否启用图形验证码。
     */
    @NotNull(message = "是否启用图形验证码不能为空")
    private Boolean captchaEnabled;

    /**
     * 触发图形验证码前允许的连续认证失败次数。
     */
    @Min(value = 0, message = "验证码触发失败次数不能小于0")
    private Integer captchaFailureThreshold;

    /**
     * 图形验证码有效期，单位为秒。
     */
    @Min(value = 1, message = "验证码有效期不能小于1秒")
    private Integer captchaTtlSeconds;

    /**
     * 单个图形验证码允许的最大错误次数。
     */
    @Min(value = 1, message = "验证码错误次数限制不能小于1")
    private Integer captchaErrorLimit;

    /**
     * 失败统计周期内允许的最大认证失败次数。
     */
    @NotNull(message = "最大失败次数不能为空")
    @Min(value = 1, message = "最大失败次数不能小于1")
    private Integer maxFailureCount;

    /**
     * 认证失败次数统计周期，单位为分钟。
     */
    @NotNull(message = "失败统计周期不能为空")
    @Min(value = 1, message = "失败统计周期不能小于1分钟")
    private Integer failureWindowMinutes;

    /**
     * 达到最大失败次数后的账号锁定时长，单位为分钟。
     */
    @NotNull(message = "锁定时长不能为空")
    @Min(value = 1, message = "锁定时长不能小于1分钟")
    private Integer lockMinutes;

    /**
     * 账号被锁定时是否通知用户。
     */
    @NotNull(message = "是否通知用户不能为空")
    private Boolean notifyUser;

    /**
     * 是否记录认证风险日志。
     */
    @NotNull(message = "是否记录风险日志不能为空")
    private Boolean riskLogEnabled;

    /**
     * 是否启用多因素认证。
     */
    @NotNull(message = "是否启用MFA不能为空")
    private Boolean mfaEnabled;

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
    @NotNull(message = "是否允许记住设备不能为空")
    private Boolean rememberDeviceEnabled;

    /**
     * 已验证设备的免验证有效天数。
     */
    @Min(value = 0, message = "记住设备天数不能小于0")
    @Max(value = 365, message = "记住设备天数不能大于365")
    private Integer rememberDeviceDays;

    /**
     * 是否启用登录IP限制。
     */
    @NotNull(message = "是否限制登录IP不能为空")
    private Boolean ipRestrictionEnabled;

    /**
     * 是否校验登录设备。
     */
    @NotNull(message = "是否校验设备不能为空")
    private Boolean deviceCheckEnabled;

    /**
     * 是否检测异地登录。
     */
    @NotNull(message = "是否校验异地登录不能为空")
    private Boolean remoteLoginCheckEnabled;

    /**
     * 是否检测异常时间登录。
     */
    @NotNull(message = "是否校验异常时间登录不能为空")
    private Boolean abnormalTimeCheckEnabled;

    /**
     * 策略状态，取值遵循统一状态枚举。
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 认证策略说明。
     */
    @Size(max = 512, message = "备注不能超过512个字符")
    private String description;
}
