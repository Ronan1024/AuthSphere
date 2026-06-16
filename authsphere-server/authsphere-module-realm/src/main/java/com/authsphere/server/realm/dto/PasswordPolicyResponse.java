package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 密码策略响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordPolicyResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 密码策略编码
     */
    private String code;

    /**
     * 密码策略名称
     */
    private String name;

    /**
     * 密码最小长度
     */
    private Integer minLength;

    /**
     * 密码最大长度
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
     * 状态
     */
    private Integer status;

    /**
     * 描述信息
     */
    private String description;
}
