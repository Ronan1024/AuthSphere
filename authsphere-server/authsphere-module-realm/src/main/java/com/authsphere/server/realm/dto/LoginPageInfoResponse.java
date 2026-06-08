package com.authsphere.server.realm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录页详情响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginPageInfoResponse extends LoginPageResponse {

    /**
     * 页面主标题。
     */
    private String pageTitle;

    /**
     * 页面副标题。
     */
    private String pageSubtitle;

    /**
     * Logo资源地址。
     */
    private String logoUrl;

    /**
     * 背景图资源地址。
     */
    private String backgroundUrl;

    /**
     * 默认认证方式编码，必须包含在支持的认证方式中。
     */
    private String defaultAuthMethod;

    /**
     * 是否展示忘记密码入口。
     */
    private Boolean showForgotPassword;

    /**
     * 是否展示注册入口。
     */
    private Boolean showRegister;

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
     * 是否为系统内置登录页。
     */
    private Boolean systemBuiltin;

    /**
     * 登录页备注。
     */
    private String description;

    /**
     * 引用该登录页的身份域数量。
     */
    private Integer realmReferenceCount;

    /**
     * 引用该登录页的客户端数量。
     */
    private Integer clientReferenceCount;
}
