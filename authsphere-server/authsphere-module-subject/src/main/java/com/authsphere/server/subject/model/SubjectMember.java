package com.authsphere.server.subject.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主体成员表。
 */
@Data
@TableName(value = "subject_member")
@EqualsAndHashCode(callSuper = true)
public class SubjectMember extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主体 ID。
     */
    private Long subjectId;

    /**
     * 账号 ID。
     */
    private Long accountId;

    /**
     * 成员类型：1 OWNER，2 ADMIN，3 MEMBER，4 STAFF。
     */
    private Integer memberType;

    /**
     * 主体内显示名称。
     */
    private String displayName;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 成员状态：1 启用，2 禁用，3 移除。
     */
    private Integer status;

    private LocalDateTime joinedAt;

    private Long joinedByAccountId;

    private LocalDateTime disabledAt;

    private Long disabledByAccountId;

    private LocalDateTime removedAt;

    private Long removedByAccountId;
}
