package com.authsphere.server.subject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建账号并加入主体请求。
 */
@Data
public class SubjectMemberCreateAccountRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名不能超过64个字符")
    private String username;

    @Size(max = 32, message = "手机号不能超过32个字符")
    private String mobile;

    @Size(max = 128, message = "邮箱不能超过128个字符")
    private String email;

    @NotBlank(message = "初始密码不能为空")
    private String password;

    @NotNull(message = "成员类型不能为空")
    private Integer memberType;

    @Size(max = 128, message = "成员显示名不能超过128个字符")
    private String displayName;

    @Size(max = 512, message = "备注不能超过512个字符")
    private String remark;
}
