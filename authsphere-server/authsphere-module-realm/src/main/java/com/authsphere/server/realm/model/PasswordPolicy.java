package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份域密码策略。
 */
@Data
@TableName(value = "password_policy")
@EqualsAndHashCode(callSuper = true)
public class PasswordPolicy extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 策略编码。
     */
    private String code;

    /**
     * 策略名称。
     */
    private String name;

    /**
     * 最小密码长度。
     */
    private Integer minLength;

    /**
     * 最大密码长度。
     */
    private Integer maxLength;

    /**
     * 是否要求大写字母。
     */
    private Boolean requireUppercase;

    /**
     * 是否要求小写字母。
     */
    private Boolean requireLowercase;

    /**
     * 是否要求数字。
     */
    private Boolean requireDigit;

    /**
     * 是否要求特殊字符。
     */
    private Boolean requireSpecialChar;

    /**
     * 是否禁止密码包含用户名。
     */
    private Boolean disallowUsername;

    /**
     * 历史密码不可重复次数。
     */
    private Integer historyCount;

    /**
     * 密码有效期天数，0 表示不限制。
     */
    private Integer expireDays;

    /**
     * 连续失败锁定阈值，0 表示不启用。
     */
    private Integer retryLimit;

    /**
     * 锁定分钟数，0 表示不自动解锁。
     */
    private Integer lockMinutes;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 描述。
     */
    private String description;
}
