package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号唯一性规则模板。
 */
@Data
@TableName(value = "unique_policy")
@EqualsAndHashCode(callSuper = true)
public class UniquePolicy extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private Boolean usernameUnique;

    private Boolean emailUnique;

    private Boolean mobileUnique;

    private Boolean caseSensitive;

    private Boolean allowEmailReuse;

    private Boolean allowMobileReuse;

    private Integer status;

    private String description;
}
