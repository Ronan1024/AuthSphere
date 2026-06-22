package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份域与认证方式关联。
 *
 * <p>身份域支持多个认证方式，但默认认证方式、MFA 方式等仍然直接挂在 realm 主表。
 * 该表只负责保存“当前身份域允许哪些认证方式被选择”。</p>
 */
@Data
@TableName("realm_auth_method_rel")
@EqualsAndHashCode(callSuper = true)
public class RealmAuthMethodRel extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 身份域主键。
     */
    private Long realmId;

    /**
     * 认证方式主键。
     */
    private Long authMethodId;
}
