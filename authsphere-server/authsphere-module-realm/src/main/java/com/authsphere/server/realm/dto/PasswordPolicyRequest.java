package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 密码策略保存请求。
 */
@Data
public class PasswordPolicyRequest {

    /**
     * 策略编码。
     */
    @NotEmpty(message = "密码策略编码不能为空")
    private String code;

    /**
     * 策略名称。
     */
    @NotEmpty(message = "密码策略名称不能为空")
    private String name;

    /**
     * 最小密码长度。
     */
    @NotNull(message = "最小密码长度不能为空")
    @Min(value = 6, message = "最小密码长度不能小于6")
    @Max(value = 128, message = "最小密码长度不能大于128")
    private Integer minLength;

    /**
     * 最大密码长度。
     */
    @NotNull(message = "最大密码长度不能为空")
    @Min(value = 6, message = "最大密码长度不能小于6")
    @Max(value = 256, message = "最大密码长度不能大于256")
    private Integer maxLength;

    @NotNull(message = "是否要求大写字母不能为空")
    private Boolean requireUppercase;

    @NotNull(message = "是否要求小写字母不能为空")
    private Boolean requireLowercase;

    @NotNull(message = "是否要求数字不能为空")
    private Boolean requireDigit;

    @NotNull(message = "是否要求特殊字符不能为空")
    private Boolean requireSpecialChar;

    @NotNull(message = "是否禁止包含用户名不能为空")
    private Boolean disallowUsername;

    /**
     * 历史密码不可重复次数。
     */
    @NotNull(message = "历史密码次数不能为空")
    @Min(value = 0, message = "历史密码次数不能小于0")
    @Max(value = 24, message = "历史密码次数不能大于24")
    private Integer historyCount;

    /**
     * 密码有效期天数，0 表示不限制。
     */
    @NotNull(message = "密码有效期不能为空")
    @Min(value = 0, message = "密码有效期不能小于0")
    @Max(value = 3650, message = "密码有效期不能大于3650天")
    private Integer expireDays;

    /**
     * 连续失败锁定阈值，0 表示不启用。
     */
    @NotNull(message = "连续失败锁定阈值不能为空")
    @Min(value = 0, message = "连续失败锁定阈值不能小于0")
    @Max(value = 20, message = "连续失败锁定阈值不能大于20")
    private Integer retryLimit;

    /**
     * 锁定分钟数，0 表示不自动解锁。
     */
    @NotNull(message = "锁定分钟数不能为空")
    @Min(value = 0, message = "锁定分钟数不能小于0")
    @Max(value = 10080, message = "锁定分钟数不能大于10080分钟")
    private Integer lockMinutes;

    /**
     * 描述。
     */
    private String description;
}
