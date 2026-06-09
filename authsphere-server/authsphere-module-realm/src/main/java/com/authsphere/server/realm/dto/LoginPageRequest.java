package com.authsphere.server.realm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 登录页保存请求。
 */
@Data
public class LoginPageRequest {

    /**
     * 登录页编码，全局唯一。
     */
    @NotEmpty(message = "登录页编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,63}$", message = "登录页编码需以小写字母开头，仅支持小写字母、数字和下划线")
    private String code;

    /**
     * 登录页名称。
     */
    @NotEmpty(message = "登录页名称不能为空")
    @Size(max = 128, message = "登录页名称不能超过128个字符")
    private String name;

    /**
     * 适用身份域类型ID，为空时表示通用登录页。
     */
    private Long applicableRealmTypeId;

    /**
     * 页面主标题。
     */
    @NotEmpty(message = "页面标题不能为空")
    @Size(max = 128, message = "页面标题不能超过128个字符")
    private String pageTitle;

    /**
     * 页面副标题。
     */
    @Size(max = 255, message = "页面副标题不能超过255个字符")
    private String pageSubtitle;

    /**
     * Logo资源地址。
     */
    @Size(max = 512, message = "Logo地址不能超过512个字符")
    private String logoUrl;

    /**
     * 背景图资源地址。
     */
    @Size(max = 512, message = "背景图地址不能超过512个字符")
    private String backgroundUrl;

    /**
     * 支持的认证方式编码集合。
     */
    @NotEmpty(message = "登录方式不能为空")
    private List<String> authMethods;

    /**
     * 默认认证方式编码，必须包含在支持的认证方式中。
     */
    @NotEmpty(message = "默认登录方式不能为空")
    private String defaultAuthMethod;

    /**
     * 是否展示忘记密码入口。
     */
    @NotNull(message = "是否显示忘记密码不能为空")
    private Boolean showForgotPassword;

    /**
     * 忘记密码入口跳转地址，展示入口时必填。
     */
    @Size(max = 512, message = "忘记密码跳转地址不能超过512个字符")
    private String forgotPasswordUrl;

    /**
     * 是否展示注册入口。
     */
    @NotNull(message = "是否显示注册入口不能为空")
    private Boolean showRegister;

    /**
     * 注册入口跳转地址，展示入口时必填。
     */
    @Size(max = 512, message = "注册入口跳转地址不能超过512个字符")
    private String registerUrl;

    /**
     * 是否展示第三方登录入口。
     */
    @NotNull(message = "是否显示第三方登录不能为空")
    private Boolean showThirdPartyLogin;

    /**
     * 登录成功后的默认跳转地址。
     */
    @Size(max = 512, message = "登录成功跳转地址不能超过512个字符")
    private String successRedirectUrl;

    /**
     * 登录失败提示模式。
     */
    @NotEmpty(message = "登录失败提示方式不能为空")
    private String failurePromptMode;

    /**
     * 是否为默认登录页。
     */
    @NotNull(message = "是否默认登录页不能为空")
    private Boolean defaultPage;

    /**
     * 登录页状态。
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 登录页备注。
     */
    @Size(max = 512, message = "备注不能超过512个字符")
    private String description;
}
