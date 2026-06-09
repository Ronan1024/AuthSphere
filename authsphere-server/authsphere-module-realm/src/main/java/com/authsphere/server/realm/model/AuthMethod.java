package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证方式，定义可被登录页和认证策略引用的认证能力及其连接参数。
 */
@Data
@TableName("auth_method")
@EqualsAndHashCode(callSuper = true)
public class AuthMethod extends BaseDataBaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    /**
     * 可用位置编码，使用逗号分隔。
     */
    private String positions;

    private String applicableRange;

    /**
     * 认证方式参数JSON。敏感参数仅在持久化层保存，接口响应由领域服务脱敏。
     */
    private String paramsJson;

    private Integer sortNo;

    private Boolean systemBuiltin;

    private Integer status;

    private String description;

    @TableLogic
    private Boolean deleted;
}
