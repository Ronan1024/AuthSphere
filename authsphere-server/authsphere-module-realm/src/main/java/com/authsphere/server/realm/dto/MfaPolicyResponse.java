package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MFA 策略模板响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MfaPolicyResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
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
