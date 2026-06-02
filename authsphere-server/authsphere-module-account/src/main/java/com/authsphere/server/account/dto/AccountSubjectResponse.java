package com.authsphere.server.account.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号加入主体响应。
 */
@Data
public class AccountSubjectResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    private String subjectCode;

    private String subjectName;

    private String subjectTypeName;

    private String rootSubjectName;

    private Integer memberType;

    private String memberTypeName;

    private Integer memberStatus;

    private Integer subjectStatus;

    private LocalDateTime joinedAt;
}
