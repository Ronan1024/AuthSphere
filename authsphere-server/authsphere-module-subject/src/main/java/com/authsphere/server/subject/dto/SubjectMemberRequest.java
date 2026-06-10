package com.authsphere.server.subject.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 主体成员新增 / 编辑请求。
 */
@Data
public class SubjectMemberRequest {

    @NotNull(message = "账号ID不能为空")
    private Long accountId;

    @NotNull(message = "成员类型不能为空")
    private Integer memberType;

    @Size(max = 128, message = "成员显示名不能超过128个字符")
    private String displayName;

    @Size(max = 512, message = "备注不能超过512个字符")
    private String remark;
}
