package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 认证策略列表响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthPolicyResponse extends BaseDataBaseModel {

    /**
     * 认证策略主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 认证策略编码。
     */
    private String code;

    /**
     * 认证策略名称。
     */
    private String name;

    /**
     * 策略允许使用的登录方式编码集合。
     */
    private List<String> authMethods;

    /**
     * 是否启用图形验证码。
     */
    private Boolean captchaEnabled;

    /**
     * 是否启用多因素认证。
     */
    private Boolean mfaEnabled;

    /**
     * 失败统计周期内允许的最大认证失败次数。
     */
    private Integer maxFailureCount;

    /**
     * 达到最大失败次数后的账号锁定时长，单位为分钟。
     */
    private Integer lockMinutes;

    /**
     * 策略状态，取值遵循统一状态枚举。
     */
    private Integer status;

    /**
     * 身份域与客户端引用该策略的总数量。
     */
    private Integer referenceCount;

    /**
     * 数据库存储的登录方式逗号分隔文本，仅用于列表查询结果转换。
     */
    @JsonIgnore
    private String authMethodsText;
}
