package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 密码策略模板分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordPolicyPageRequest extends PageRequest {

    /**
     * 策略编码。
     */
    private String code;

    /**
     * 策略名称。
     */
    private String name;

    /**
     * 状态。
     */
    private Integer status;
}
