package com.authsphere.server.realm.model;

import com.authsphere.server.common.model.BaseDataBaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录页配置。
 */
@Data
@TableName("login_page")
@EqualsAndHashCode(callSuper = true)
public class LoginPage extends BaseDataBaseModel {

    /**
     * 登录页ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录页编号。
     */
    private String code;

    /**
     * 登录页名称。
     */
    private String name;

    /**
     * 登录页适用的身份域类型ID。
     */
    private Long applicableRealmTypeId;

    /**
     * 登录页标题。
     */
    private String pageTitle;

    /**
     * 登录页副标题。
     */
    private String pageSubtitle;

    /**
     * 登录页Logo URL。
     */
    private String logoUrl;

    /**
     * 登录页背景图片URL。
     */
    private String backgroundUrl;

    /**
     * Logo OSS对象标识。
     */
    private String logoObjectKey;

    /**
     * 背景图 OSS对象标识。
     */
    private String backgroundObjectKey;

    /**
     * 页面布局模式。
     */
    private String layoutMode;

    /**
     * 主题配置 JSON。
     */
    private String themeConfigJson;

    /**
     * 组件展示配置 JSON。
     */
    private String componentConfigJson;

    /**
     * 微前端远程入口地址。
     */
    private String microFrontendUrl;

    /**
     * 逗号分隔的登录方式编码。
     */
    private String authMethods;

    /**
     * 默认登录方式编码。
     */
    private String defaultAuthMethod;

    /**
     * 是否展示忘记密码入口。
     */
    private Boolean showForgotPassword;

    /**
     * 忘记密码入口跳转地址。
     */
    private String forgotPasswordUrl;

    /**
     * 是否展示注册入口。
     */
    private Boolean showRegister;

    /**
     * 注册入口跳转地址。
     */
    private String registerUrl;

    /**
     * 是否展示第三方登录入口。
     */
    private Boolean showThirdPartyLogin;

    /**
     * 登录成功后的默认跳转地址。
     */
    private String successRedirectUrl;

    /**
     * 登录失败提示模式。
     */
    private String failurePromptMode;

    /**
     * 是否为默认登录页。
     */
    private Boolean defaultPage;

    /**
     * 是否为系统内置登录页。
     */
    private Boolean systemBuiltin;
    /**
     * 登录页状态。
     */
    private Integer status;

    /**
     * 登录页备注。
     */
    private String description;

    /**
     * 逻辑删除标识。
     */
    @TableLogic
    private Boolean deleted;
}
