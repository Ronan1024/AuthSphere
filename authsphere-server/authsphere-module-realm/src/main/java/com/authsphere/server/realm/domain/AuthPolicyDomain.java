package com.authsphere.server.realm.domain;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.AuthPolicyConvert;
import com.authsphere.server.realm.dto.AuthPolicyInfoResponse;
import com.authsphere.server.realm.dto.AuthPolicyRequest;
import com.authsphere.server.realm.dto.AuthPolicyResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.AuthPolicyMapper;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.AuthPolicy;
import com.authsphere.server.realm.model.Realm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 认证策略领域服务，统一处理策略校验、引用检查和响应组装。
 */
@Service
@RequiredArgsConstructor
public class AuthPolicyDomain {

    private final AuthPolicyMapper authPolicyMapper;
    private final RealmMapper realmMapper;

    /**
     * 根据ID查询认证策略，不存在时抛出业务异常。
     *
     * @param id 认证策略主键
     * @return 认证策略实体
     */
    public AuthPolicy findById(Long id) {
        AuthPolicy policy = authPolicyMapper.selectById(id);
        if (policy == null) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_DATA_ERROR);
        }
        return policy;
    }

    /**
     * 规范化并校验认证策略保存参数，确保登录方式、状态和各安全配置满足业务约束。
     *
     * @param request 认证策略保存参数
     */
    public void normalizeAndValidateRequest(AuthPolicyRequest request) {
        List<String> authMethods = normalize(request.getAuthMethods());
        if (authMethods.isEmpty() || !authMethods.contains(request.getDefaultAuthMethod())) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_PARAM_ERROR);
        }
        if (!StatusEnum.NORMAL.getCode().equals(request.getStatus())
                && !StatusEnum.DISABLED.getCode().equals(request.getStatus())) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_PARAM_ERROR);
        }
        validateCaptcha(request);
        validateMfa(request);
        validateApplicableRealm(request.getApplicableRealmId());
        request.setAuthMethods(authMethods);
        request.setMfaTriggers(normalize(request.getMfaTriggers()));
        request.setMfaMethods(normalize(request.getMfaMethods()));
    }



    /**
     * 校验策略编码是否可用。
     *
     * @param currentId 编辑场景下需要排除的当前策略主键
     * @param code 待校验的认证策略编码
     */
    public void checkCodeAvailable(Long currentId, String code) {
        if (value(authPolicyMapper.countByCode(code, currentId)) > 0) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_CODE_EXISTS);
        }
    }

    /**
     * 校验策略未被身份域或客户端引用。
     *
     * @param policyId 认证策略主键
     */
    public void checkNotReferenced(Long policyId) {
        if (value(authPolicyMapper.countRealmReferences(policyId)) > 0
                || value(authPolicyMapper.countClientReferences(policyId)) > 0) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_REFERENCED);
        }
    }

    /**
     * 校验认证策略允许删除。
     *
     * @param policy 待删除的认证策略实体
     */
    public void checkCanDelete(AuthPolicy policy) {
        if (Boolean.TRUE.equals(policy.getSystemBuiltin())) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_SYSTEM_BUILTIN);
        }
        checkNotReferenced(policy.getId());
    }

    /**
     * 校验认证策略存在且已启用。
     *
     * @param id 认证策略主键
     */
    public void checkEnabled(Long id) {
        AuthPolicy authPolicy = findById(id);
        if (!StatusEnum.NORMAL.getCode().equals(authPolicy.getStatus())) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_DISABLED);
        }
    }

    /**
     * 为列表响应转换认证方式字段。
     *
     * @param responses 认证策略列表查询结果
     */
    public void enrichListResponses(List<AuthPolicyResponse> responses) {
        responses.forEach(response ->
                response.setAuthMethods(AuthPolicyConvert.INSTANCE.split(response.getAuthMethodsText())));
    }

    /**
     * 组装认证策略详情及引用影响范围。
     *
     * @param policy 认证策略实体
     * @return 已补充适用身份域和引用影响范围的详情响应
     */
    public AuthPolicyInfoResponse buildInfoResponse(AuthPolicy policy) {
        AuthPolicyInfoResponse response = AuthPolicyConvert.INSTANCE.infoResponse(policy);
        int realmReferenceCount = value(authPolicyMapper.countRealmReferences(policy.getId()));
        int clientReferenceCount = value(authPolicyMapper.countClientReferences(policy.getId()));
        response.setRealmReferenceCount(realmReferenceCount);
        response.setClientReferenceCount(clientReferenceCount);
        response.setReferenceCount(realmReferenceCount + clientReferenceCount);
        response.setReferences(authPolicyMapper.listReferences(policy.getId()));
        if (policy.getApplicableRealmId() != null) {
            Realm realm = realmMapper.selectById(policy.getApplicableRealmId());
            response.setApplicableRealmName(realm == null ? null : realm.getName());
        }
        return response;
    }

    /**
     * 校验图形验证码配置；启用验证码时必须提供触发阈值、有效期和错误次数限制。
     *
     * @param request 认证策略保存参数
     */
    private void validateCaptcha(AuthPolicyRequest request) {
        if (Boolean.TRUE.equals(request.getCaptchaEnabled())
                && (request.getCaptchaFailureThreshold() == null
                || request.getCaptchaTtlSeconds() == null
                || request.getCaptchaErrorLimit() == null)) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_PARAM_ERROR);
        }
    }

    /**
     * 校验多因素认证及记住设备配置。
     * 启用多因素认证时必须配置触发场景和认证方式，启用记住设备时有效天数必须大于零。
     *
     * @param request 认证策略保存参数
     */
    private void validateMfa(AuthPolicyRequest request) {
        if (Boolean.TRUE.equals(request.getMfaEnabled())
                && (normalize(request.getMfaTriggers()).isEmpty()
                || normalize(request.getMfaMethods()).isEmpty())) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_PARAM_ERROR);
        }
        if (Boolean.TRUE.equals(request.getRememberDeviceEnabled())
                && (request.getRememberDeviceDays() == null || request.getRememberDeviceDays() <= 0)) {
            throw new BizException(RealmErrorCode.AUTH_POLICY_PARAM_ERROR);
        }
    }

    /**
     * 校验限定适用的身份域是否存在。
     *
     * @param realmId 策略限定适用的身份域主键；为空时不校验
     */
    private void validateApplicableRealm(Long realmId) {
        if (realmId != null && realmMapper.selectById(realmId) == null) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR);
        }
    }

    /**
     * 规范化字符串集合，移除空值、空白项和重复项。
     *
     * @param values 待规范化的字符串集合
     * @return 不包含空值和重复项的字符串集合
     */
    private List<String> normalize(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }

    /**
     * 将数据库统计结果转换为非空整数。
     *
     * @param count 数据库统计结果
     * @return 统计结果；为空时返回零
     */
    private int value(Integer count) {
        return count == null ? 0 : count;
    }
}
