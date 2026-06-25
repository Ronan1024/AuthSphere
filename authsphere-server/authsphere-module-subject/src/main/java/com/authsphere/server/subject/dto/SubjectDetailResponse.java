package com.authsphere.server.subject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主体详情响应。
 */
@Data
public class SubjectDetailResponse {

    /**
     * 主体 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 主体类型 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectTypeId;

    /**
     * 主体类型编码。
     */
    private String subjectTypeCode;

    /**
     * 主体类型名称。
     */
    private String subjectTypeName;

    /**
     * 所属身份域 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 主体编码。
     */
    private String code;

    /**
     * 主体名称。
     */
    private String name;

    /**
     * 主体状态。
     */
    private Integer status;

    /**
     * 数据边界根主体 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rootSubjectId;

    /**
     * 数据边界根主体编码。
     */
    private String rootSubjectCode;

    /**
     * 数据边界根主体名称。
     */
    private String rootSubjectName;

    /**
     * 父级主体 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentSubjectId;

    /**
     * 父级主体编码。
     */
    private String parentSubjectCode;

    /**
     * 父级主体名称。
     */
    private String parentSubjectName;

    /**
     * 是否为数据边界根主体。
     */
    private Boolean isRoot;

    /**
     * 是否系统内置主体。
     */
    private Boolean builtIn;

    /**
     * 主体描述。
     */
    private String description;

    /**
     * 成员账号数量。
     */
    private Integer memberCount;

    /**
     * 开通应用数量。
     */
    private Integer appCount;

    /**
     * 下级主体数量。
     */
    private Integer childCount;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
