package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MFA 策略模板分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MfaPolicyPageRequest extends PageRequest {

    private String code;

    private String name;

    private Integer status;
}
