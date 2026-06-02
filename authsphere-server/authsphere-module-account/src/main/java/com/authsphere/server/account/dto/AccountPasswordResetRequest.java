package com.authsphere.server.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员重置账号密码请求。
 */
@Data
public class AccountPasswordResetRequest {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    private Boolean forceReset;
}
