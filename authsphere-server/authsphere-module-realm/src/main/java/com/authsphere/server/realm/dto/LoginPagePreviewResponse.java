package com.authsphere.server.realm.dto;

import lombok.Data;

import java.util.List;

/**
 * 登录页预览响应。
 */
@Data
public class LoginPagePreviewResponse {

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
     * 支持的认证方式编码集合。
     */
    private List<String> authMethods;

    /**
     * 默认认证方式编码。
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
     * 登录失败提示模式。
     */
    private String failurePromptMode;
}
