package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用端实例分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppClientInstancePageRequest extends PageRequest {

    private Long appInstanceId;

    private Long appClientId;

    private String appCode;

    private String clientCode;

    private String clientInstanceCode;

    private String clientInstanceName;

    private Long ownerSubjectId;

    private Integer status;
}
