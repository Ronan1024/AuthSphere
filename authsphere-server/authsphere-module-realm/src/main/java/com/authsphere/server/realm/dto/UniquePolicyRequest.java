package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账号唯一性规则模板保存请求。
 */
@Data
public class UniquePolicyRequest {

    @NotEmpty(message = "账号唯一性规则编码不能为空")
    private String code;

    @NotEmpty(message = "账号唯一性规则名称不能为空")
    private String name;

    @NotNull(message = "用户名是否唯一不能为空")
    private Boolean usernameUnique;

    @NotNull(message = "邮箱是否唯一不能为空")
    private Boolean emailUnique;

    @NotNull(message = "手机号是否唯一不能为空")
    private Boolean mobileUnique;

    @NotNull(message = "是否大小写敏感不能为空")
    private Boolean caseSensitive;

    @NotNull(message = "是否允许邮箱复用不能为空")
    private Boolean allowEmailReuse;

    @NotNull(message = "是否允许手机号复用不能为空")
    private Boolean allowMobileReuse;

    private String description;
}
