package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主体成员响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectMemberResponse extends BaseDataBaseModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    private String subjectCode;

    private String subjectName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long realmId;

    private String realmCode;

    private String realmName;

    private String username;

    private String mobile;

    private String email;

    private Integer memberType;

    private String memberTypeName;

    private String displayName;

    private String remark;

    private Integer memberStatus;

    private Integer accountStatus;

    private LocalDateTime joinedAt;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long joinedByAccountId;

    private String joinedByUsername;

    private LocalDateTime disabledAt;

    private LocalDateTime removedAt;
}
