package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 认证方式保存请求。
 */
@Data
public class AuthMethodRequest {

    @NotBlank(message = "认证方式编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,63}$", message = "认证方式编码需以小写字母开头，仅支持小写字母、数字和下划线")
    private String code;

    @NotBlank(message = "认证方式名称不能为空")
    @Size(max = 128, message = "认证方式名称不能超过128个字符")
    private String name;

    @NotEmpty(message = "至少选择一个可用位置")
    private List<String> positions;

    @Size(max = 128, message = "适用范围不能超过128个字符")
    private String applicableRange;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortNo;

    @Size(max = 512, message = "备注不能超过512个字符")
    private String description;

    /**
     * 不同认证方式的扩展参数。具体参数由认证执行器解释。
     */
    private Map<String, Object> params;
}
