package com.authsphere.server.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户账号
 *
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
public class AccountCreateRequest {

    /**
     * 身份域id
     */
    @NotNull(message = "身份域不能为空")
    private Long realmId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 255, message = "用户名不能超过255个字符")
    private String username;

    /**
     * 昵称。
     */
    @Size(max = 128, message = "昵称不能超过128个字符")
    private String nickname;

    /**
     * 头像。
     */
    @Size(max = 512, message = "头像不能超过512个字符")
    private String avatar;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    /**
     * 初始密码。
     */
    private String password;

    /**
     * 账号状态：1.启用 2.锁定 3.禁用
     */
    private Integer status;
}
