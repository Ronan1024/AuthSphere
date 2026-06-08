package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordPolicyListResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
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
     * 密码最小长度。
     */
    private Integer minLength;

    /**
     * 密码最大长度。
     */
    private Integer maxLength;


    /**
     * 有效期
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
}
