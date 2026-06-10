package com.authsphere.server.subject.dto;

import com.authsphere.server.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主体成员分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectMemberPageRequest extends PageRequest {

    private Long subjectId;

    private Long accountId;

    /**
     * 账号关键词：用户名 / 手机号 / 邮箱。
     */
    private String keyword;

    private Integer memberType;

    private Integer memberStatus;

    private Integer accountStatus;

    private Long realmId;

    private LocalDateTime joinedStartAt;

    private LocalDateTime joinedEndAt;
}
