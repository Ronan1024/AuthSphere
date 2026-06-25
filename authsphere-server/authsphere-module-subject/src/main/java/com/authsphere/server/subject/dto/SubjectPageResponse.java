package com.authsphere.server.subject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主体列表分页响应。
 */
@Data
public class SubjectPageResponse {

    /**
     * 主体 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 主体编码。
     */
    private String code;

    /**
     * 主体名称。
     */
    private String name;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 是否内置主体。
     */
    private Boolean builtIn;

    /**
     * 主体类型编码。
     */
    private String subjectTypeCode;

    /**
     * 所属身份域 ID
     */
    private Long realmId;

    /**
     * 所属身份域编码。
     */
    private String realmCode;

    /**
     * 所属身份域名称。
     */
    private String realmName;

    /**
     * 父主体Id
     */
    private Long parentId;

    /**
     * 父主体名称。
     */
    private String parentName;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 签约数量
     */
    private Integer contractCount;

    /**
     * 客户端数量
     */
    private Integer clientCount;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
