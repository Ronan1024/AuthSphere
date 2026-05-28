package com.authsphere.server.realm.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class RealmPageResponse extends BaseDataBaseModel {

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
     * 是否允许注册
     */
    private Boolean registerEnabled;

    /**
     * 状态
     */
    private Integer status;
}
