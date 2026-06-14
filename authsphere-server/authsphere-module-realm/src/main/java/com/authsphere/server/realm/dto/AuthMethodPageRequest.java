package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证方式分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthMethodPageRequest extends PageRequest {
    /**
     * 认证方式名称
     */
    private String name;

    /**
     * 认证方式编码
     */
    private String code;

    /**
     * 认证方式位置
     */
    private String position;

    /**
     * 认证方式状态
     */
    private Integer status;
}
