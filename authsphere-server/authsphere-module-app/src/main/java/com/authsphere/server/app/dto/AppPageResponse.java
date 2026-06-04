package com.authsphere.server.app.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppPageResponse extends BaseDataBaseModel {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 描述
     */
    private String description;


    /**
     * 应用编码
     */
    private String appCode;


    /**
     * 应用类型
     */
    private String appType;


    /**
     * 状态
     */
    private Integer status;

    /**
     * 客户端名称
     */
    private List<String> clientName;

}
