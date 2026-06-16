package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.PageRequest;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealmPageRequest extends PageRequest {

    /**
     * 身份域编号
     */
    private String code;

    /**
     * 身份域名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 身份域类型
     */
    @JsonAlias("typeCategoryId")
    private Long realmTypeId;
}
