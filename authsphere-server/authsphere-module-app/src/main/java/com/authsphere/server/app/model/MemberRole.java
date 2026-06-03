package com.authsphere.server.app.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主体成员角色授权表。
 */
@Data
@TableName(value = "iam_member_role")
@EqualsAndHashCode(callSuper = true)
public class MemberRole extends BaseDataBaseModel {

    @TableId
    private Long id;

    private Long memberId;

    private Long roleId;

    private Long appInstanceId;

    private Long clientInstanceId;

    private Long grantedByAccountId;

    private LocalDateTime grantedAt;

    private Integer status;
}
