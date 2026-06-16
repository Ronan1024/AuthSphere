package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * MFA 策略模板保存请求。
 */
@Data
public class MfaPolicyRequest {

    @NotEmpty(message = "MFA 策略编码不能为空")
    private String code;

    @NotEmpty(message = "MFA 策略名称不能为空")
    private String name;

    @NotNull(message = "是否强制 MFA 不能为空")
    private Boolean required;

    @NotNull(message = "是否启用短信 MFA 不能为空")
    private Boolean smsEnabled;

    @NotNull(message = "是否启用邮箱 MFA 不能为空")
    private Boolean emailEnabled;

    @NotNull(message = "是否启用 TOTP MFA 不能为空")
    private Boolean totpEnabled;

    @NotNull(message = "是否启用 WebAuthn MFA 不能为空")
    private Boolean webauthnEnabled;

    @NotNull(message = "是否记住设备不能为空")
    private Boolean rememberDeviceEnabled;

    @NotNull(message = "记住设备天数不能为空")
    @Min(value = 0, message = "记住设备天数不能小于0")
    @Max(value = 365, message = "记住设备天数不能大于365")
    private Integer rememberDeviceDays;

    @NotNull(message = "是否启用风险触发 MFA 不能为空")
    private Boolean riskBasedEnabled;

    private String description;
}
