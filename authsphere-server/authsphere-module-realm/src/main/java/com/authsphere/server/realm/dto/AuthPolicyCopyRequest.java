package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 复制认证策略请求。
 */
@Data
public class AuthPolicyCopyRequest {

    /**
     * 新认证策略编码，用于唯一标识复制后生成的策略。
     */
    @NotBlank(message = "新认证策略编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,63}$", message = "认证策略编码需以小写字母开头，仅支持小写字母、数字和下划线")
    private String code;

    /**
     * 新认证策略名称，用于页面展示复制后生成的策略。
     */
    @NotBlank(message = "新认证策略名称不能为空")
    @Size(max = 128, message = "认证策略名称不能超过128个字符")
    private String name;
}
