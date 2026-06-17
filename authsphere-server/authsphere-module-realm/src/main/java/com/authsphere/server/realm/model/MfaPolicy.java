package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MFA 策略模板。
 */
@Data
@TableName(value = "mfa_policy")
@EqualsAndHashCode(callSuper = true)
public class MfaPolicy extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private Boolean required;

    private Boolean smsEnabled;

    private Boolean emailEnabled;

    private Boolean totpEnabled;

    private Boolean webauthnEnabled;

    private Boolean rememberDeviceEnabled;

    private Integer rememberDeviceDays;

    private Boolean riskBasedEnabled;

    private Integer status;

    private String description;
}
