package com.authsphere.server.realm.domain;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.LoginPageConvert;
import com.authsphere.server.realm.dto.AuthMethodInfoResponse;
import com.authsphere.server.realm.dto.LoginPageInfoResponse;
import com.authsphere.server.realm.dto.LoginPagePreviewResponse;
import com.authsphere.server.realm.dto.LoginPageRequest;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.LoginPageMapper;
import com.authsphere.server.realm.mapper.RealmTypeMapper;
import com.authsphere.server.realm.model.LoginPage;
import com.authsphere.server.realm.model.RealmType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 登录页领域服务，统一承载登录页校验、引用检查和响应组装逻辑。
 */
@Service
@RequiredArgsConstructor
public class LoginPageDomain {

    /**
     * 认证方式编码与展示名称映射。
     */
    private static final Map<String, String> AUTH_METHOD_NAMES = createAuthMethodNames();

    /**
     * 认证方式编码与稳定标识映射。
     */
    private static final Map<String, Long> AUTH_METHOD_IDS = createAuthMethodIds();

    private final LoginPageMapper loginPageMapper;
    private final RealmTypeMapper realmTypeMapper;

    /**
     * 根据ID查询登录页，不存在时抛出业务异常。
     *
     * @param id 登录页ID
     * @return 登录页实体
     */
    public LoginPage findById(Long id) {
        LoginPage loginPage = loginPageMapper.selectById(id);
        if (loginPage == null) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_DATA_ERROR);
        }
        return loginPage;
    }

    /**
     * 规范化并校验登录页保存参数。
     *
     * @param request 登录页保存参数
     */
    public void normalizeAndValidateRequest(LoginPageRequest request) {
        List<String> authMethods = request.getAuthMethods().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(method -> !method.isEmpty())
                .distinct()
                .toList();
        if (authMethods.isEmpty() || !authMethods.contains(request.getDefaultAuthMethod())) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_AUTH_METHOD_ERROR);
        }
        if (!StatusEnum.NORMAL.getCode().equals(request.getStatus())
                && !StatusEnum.DISABLED.getCode().equals(request.getStatus())) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_STATUS_ERROR);
        }
        request.setAuthMethods(authMethods);
    }

    /**
     * 校验登录页编码是否可用。
     *
     * @param currentId 当前登录页ID，新增时为空
     * @param code 登录页编码
     */
    public void checkCodeAvailable(Long currentId, String code) {
        if (getReferenceCount(loginPageMapper.countByCode(code, currentId)) > 0) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_CODE_EXISTS);
        }
    }

    /**
     * 校验适用身份域类型是否存在。
     *
     * @param realmTypeId 身份域类型ID
     */
    public void checkApplicableRealmTypeExists(Long realmTypeId) {
        if (realmTypeId != null && realmTypeMapper.selectById(realmTypeId) == null) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_DATA_ERROR);
        }
    }

    /**
     * 校验登录页未被身份域或客户端引用。
     *
     * @param loginPageId 登录页ID
     */
    public void checkNotReferenced(Long loginPageId) {
        if (getReferenceCount(loginPageMapper.countRealmReferences(loginPageId)) > 0) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_REALM_REFERENCED);
        }
        if (getReferenceCount(loginPageMapper.countClientReferences(loginPageId)) > 0) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_CLIENT_REFERENCED);
        }
    }

    /**
     * 校验登录页允许删除。系统内置登录页及被引用的登录页禁止删除。
     *
     * @param loginPage 登录页实体
     */
    public void checkCanDelete(LoginPage loginPage) {
        if (Boolean.TRUE.equals(loginPage.getSystemBuiltin())) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_SYSTEM_BUILTIN);
        }
        checkNotReferenced(loginPage.getId());
    }

    /**
     * 校验登录页存在且处于启用状态。
     *
     * @param id 登录页ID
     */
    public void checkEnabled(Long id) {
        LoginPage loginPage = findById(id);
        if (!StatusEnum.NORMAL.getCode().equals(loginPage.getStatus())) {
            throw new BizException(RealmErrorCode.LOGIN_PAGE_DISABLED);
        }
    }

//    /**
//     * 为分页列表响应补充认证方式展示信息。
//     *
//     * @param responses 登录页列表响应
//     */
//    public void enrichListResponses(List<LoginPageResponse> responses) {
//        responses.forEach(response -> response.setAuthMethod(buildAuthMethodInfo(response.getAuthMethodsText())));
//    }

    /**
     * 组装登录页详情响应。
     *
     * @param loginPage 登录页实体
     * @return 登录页详情响应
     */
    public LoginPageInfoResponse buildInfoResponse(LoginPage loginPage) {
        LoginPageInfoResponse response = LoginPageConvert.INSTANCE.infoResponse(loginPage);
        int realmReferenceCount = getReferenceCount(loginPageMapper.countRealmReferences(loginPage.getId()));
        int clientReferenceCount = getReferenceCount(loginPageMapper.countClientReferences(loginPage.getId()));
        response.setRealmReferenceCount(realmReferenceCount);
        response.setClientReferenceCount(clientReferenceCount);
        response.setReferenceCount(realmReferenceCount + clientReferenceCount);
        response.setAuthMethod(buildAuthMethodInfo(loginPage.getAuthMethods()));
        response.setApplicableRealmTypeName(findRealmTypeName(loginPage.getApplicableRealmTypeId()));
        return response;
    }

    /**
     * 组装登录页预览响应。
     *
     * @param loginPage 登录页实体
     * @return 登录页预览响应
     */
    public LoginPagePreviewResponse buildPreviewResponse(LoginPage loginPage) {
        return LoginPageConvert.INSTANCE.previewResponse(loginPage);
    }

    /**
     * 将存储层认证方式编码转换为接口展示对象。
     */
    private List<AuthMethodInfoResponse> buildAuthMethodInfo(String authMethodsText) {
        List<String> authMethods = LoginPageConvert.INSTANCE.splitAuthMethods(authMethodsText);
        if (authMethods.isEmpty()) {
            return Collections.emptyList();
        }
        List<AuthMethodInfoResponse> responses = new ArrayList<>(authMethods.size());
        for (String authMethod : authMethods) {
            AuthMethodInfoResponse response = new AuthMethodInfoResponse();
            response.setId(AUTH_METHOD_IDS.get(authMethod));
            response.setName(AUTH_METHOD_NAMES.getOrDefault(authMethod, authMethod));
            response.setDescription(authMethod);
            responses.add(response);
        }
        return responses;
    }

    /**
     * 查询适用身份域类型名称，身份域类型已删除时返回空。
     */
    private String findRealmTypeName(Long realmTypeId) {
        if (realmTypeId == null) {
            return null;
        }
        RealmType realmType = realmTypeMapper.selectById(realmTypeId);
        return realmType == null ? null : realmType.getName();
    }

    /**
     * 将数据库统计结果转换为非空基础类型。
     */
    private int getReferenceCount(Integer count) {
        return count == null ? 0 : count;
    }

    /**
     * 创建认证方式展示名称映射。
     */
    private static Map<String, String> createAuthMethodNames() {
        Map<String, String> authMethodNames = new LinkedHashMap<>();
        authMethodNames.put("password", "密码登录");
        authMethodNames.put("sms", "短信验证码");
        authMethodNames.put("email", "邮箱验证码");
        authMethodNames.put("qr", "扫码登录");
        authMethodNames.put("third_party", "第三方登录");
        authMethodNames.put("mfa", "多因素认证");
        return Collections.unmodifiableMap(authMethodNames);
    }

    /**
     * 创建认证方式稳定标识映射。
     */
    private static Map<String, Long> createAuthMethodIds() {
        Map<String, Long> authMethodIds = new LinkedHashMap<>();
        authMethodIds.put("password", 1L);
        authMethodIds.put("sms", 2L);
        authMethodIds.put("email", 3L);
        authMethodIds.put("qr", 4L);
        authMethodIds.put("third_party", 5L);
        authMethodIds.put("mfa", 6L);
        return Collections.unmodifiableMap(authMethodIds);
    }
}
