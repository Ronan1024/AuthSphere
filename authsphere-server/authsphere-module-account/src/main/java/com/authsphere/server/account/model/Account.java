package com.authsphere.server.account.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号表
 * @TableName account
 */
@Data
@TableName(value ="account")
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseDataBaseModel {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 身份域id
     */
    private Long realmId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 账号状态：1.启用 2.锁定 3.禁用
     */
    private Integer status;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 最后登录时间
     */
    private java.time.LocalDateTime lastLoginTime;
}
