package com.authsphere.server.api.model.dto.realm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
@Data
public class RealmInfoResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 身份域编码， 唯一
     */
    private String code;

    /**
     * 身份域名称
     */
    private String name;

    /**
     * 身份域类型id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long typeCategoryId;

    /**
     * 是否允许注册
     */
    private Boolean registerEnabled;

    /**
     * 独立登录页
     */
    private String loginUrl;

    /**
     * 是否开启SSO
     */
    private Boolean ssoEnabled;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
