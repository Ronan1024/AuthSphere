package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份域信息
 * @TableName realm
 */
@Data
@TableName(value ="realm")
@EqualsAndHashCode(callSuper = true)
public class Realm  extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
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
     * 身份域类型
     */
    private String typeCode;

    /**
     * 独立登录页
     */
    private String loginUrl;

    /**
     * 是否允许注册
     */
    private Boolean registerEnabled;

    /**
     * 是否开启SSO
     */
    private Boolean ssoEnabled;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 密码策略
     */
    private Long passwordPolicy;

    /**
     * MFA 策略
     */
    private Long mfaPolicy;

    /**
     * 账号唯一性规则
     */
    private Long uniquePolicy;

    /**
     * 描述
     */
    private String description;
}