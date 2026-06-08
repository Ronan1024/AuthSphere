package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证策略，定义登录认证链路、失败锁定、MFA及风险校验规则。
 */
@Data
@TableName("auth_policy")
@EqualsAndHashCode(callSuper = true)
public class AuthPolicy extends BaseDataBaseModel {

    /**
     * 认证策略ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 认证策略唯一编码。
     */
    private String code;

    /**
     * 认证策略名称。
     */
    private String name;

    /**
     * 主要适用身份域ID，为空表示通用策略。
     */
    private Long applicableRealmId;

    /**
     * 支持的登录方式编码，使用逗号分隔。
     */
    private String authMethods;

    /**
     * 默认登录方式编码。
     */
    private String defaultAuthMethod;

    /**
     * 是否启用图形验证码。
     */
    private Boolean captchaEnabled;

    /**
     * 连续失败多少次后启用图形验证码。
     */
    private Integer captchaFailureThreshold;

    /**
     * 图形验证码有效期，单位秒。
     */
    private Integer captchaTtlSeconds;

    /**
     * 图形验证码允许的最大错误次数。
     */
    private Integer captchaErrorLimit;

    /**
     * 登录最大连续失败次数。
     */
    private Integer maxFailureCount;

    /**
     * 登录失败统计周期，单位分钟。
     */
    private Integer failureWindowMinutes;

    /**
     * 账号锁定时长，单位分钟。
     */
    private Integer lockMinutes;

    /**
     * 账号锁定后是否通知用户。
     */
    private Boolean notifyUser;

    /**
     * 是否记录风险日志。
     */
    private Boolean riskLogEnabled;

    /**
     * 是否启用MFA。
     */
    private Boolean mfaEnabled;

    /**
     * MFA触发条件编码，使用逗号分隔。
     */
    private String mfaTriggers;

    /**
     * MFA认证方式编码，使用逗号分隔。
     */
    private String mfaMethods;

    /**
     * 是否允许记住可信设备。
     */
    private Boolean rememberDeviceEnabled;

    /**
     * 可信设备有效期，单位天。
     */
    private Integer rememberDeviceDays;

    /**
     * 是否启用登录IP限制。
     */
    private Boolean ipRestrictionEnabled;

    /**
     * 是否校验设备指纹。
     */
    private Boolean deviceCheckEnabled;

    /**
     * 是否校验异地登录。
     */
    private Boolean remoteLoginCheckEnabled;

    /**
     * 是否校验异常时间登录。
     */
    private Boolean abnormalTimeCheckEnabled;

    /**
     * 是否为系统内置策略。
     */
    private Boolean systemBuiltin;

    /**
     * 策略状态。
     */
    private Integer status;

    /**
     * 策略用途说明。
     */
    private String description;

    /**
     * 逻辑删除标识。
     */
    @TableLogic
    private Boolean deleted;
}
