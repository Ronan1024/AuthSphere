package com.authsphere.server.realm.domain;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.AuthMethodDetailResponse;
import com.authsphere.server.realm.dto.AuthMethodOptionResponse;
import com.authsphere.server.realm.dto.AuthMethodReferenceResponse;
import com.authsphere.server.realm.dto.AuthMethodRequest;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.enums.AuthMethodStatus;
import com.authsphere.server.realm.enums.AuthMethodTemplate;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 认证方式领域服务，统一处理参数规范化、引用校验和敏感配置脱敏。
 */
@Service
@RequiredArgsConstructor
public class AuthMethodDomain {

    private static final Set<String> SUPPORTED_POSITIONS = Set.of(
            "主登录", "MFA", "MFA 二次认证", "接口认证", "允许自动建号", "账号绑定校验", "敏感操作");
    private static final List<Set<String>> SUPPORTED_TEMPLATE_PARAM_KEYS = List.of(
            Set.of("passwordPolicy", "maxRetries", "lockDuration"),
            Set.of("totpDigits", "totpPeriod", "totpWindow"),
            Set.of("tokenTtl", "signAlg", "allowScope"),
            Set.of("smsProvider", "codeTtl", "sendInterval", "dailyLimit", "retryLimit", "templateCode"),
            Set.of("emailProvider", "emailTtl", "emailTemplate", "sendInterval"),
            Set.of("oidcIssuer", "clientId", "clientSecret", "userField", "callbackUrl"),
            Set.of("ldapUrl", "baseDn", "ldapUserField", "ldapMailField")
    );
    private static final String MASK_VALUE = "********";
    private static final String FORM_SCHEMA_KEY = "formSchema";
    private static final String FIELD_CODE_KEY = "code";

    private final AuthMethodMapper authMethodMapper;
    private final ObjectMapper objectMapper;

    /**
     * 查询认证方式，不存在时抛出业务异常。
     *
     * @param id 认证方式主键
     * @return 认证方式实体
     */
    public AuthMethod findById(Long id) {
        AuthMethod method = authMethodMapper.selectById(id);
        if (method == null) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_DATA_ERROR);
        }
        return method;
    }

    /**
     * 规范化并校验认证方式保存参数。
     *
     * @param request 保存请求
     */
    public void normalizeAndValidate(AuthMethodRequest request) {
        List<String> positions = normalize(request.getPositions());
        if (positions.isEmpty() || positions.stream().anyMatch(position -> !SUPPORTED_POSITIONS.contains(position))) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
        }
        if (!AuthMethodStatus.supports(request.getStatus())) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
        }
        request.setPositions(positions);
        request.setApplicableRange(trimToNull(request.getApplicableRange()));
        request.setDescription(trimToNull(request.getDescription()));
        request.setParams(normalizeAndValidateParams(request.getCode(), request.getParams()));
    }

    /**
     * 校验认证方式编码唯一。
     *
     * @param currentId 编辑时排除的认证方式主键
     * @param code 认证方式编码
     */
    public void checkCodeAvailable(Long currentId, String code) {
        if (value(authMethodMapper.countByCode(code, currentId)) > 0) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_CODE_EXISTS);
        }
    }

    /**
     * 校验认证方式未被认证策略引用。
     *
     * @param code 认证方式编码
     */
    public void checkNotReferenced(String code) {
        Integer counted = authMethodMapper.countReferences(code);
        if (counted > 0) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_REFERENCED);
        }
    }

    /**
     * 校验认证方式允许删除。
     *
     * @param method 认证方式实体
     */
    public void checkCanDelete(AuthMethod method) {
        if (Boolean.TRUE.equals(method.getSystemBuiltin())) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_SYSTEM_BUILTIN);
        }
        checkNotReferenced(method.getCode());
    }

    /**
     * 校验一组认证方式编码均存在且已启用。
     *
     * @param codes 认证方式编码集合
     */
    public void checkEnabledCodes(List<String> codes) {
        List<String> normalized = normalize(codes);
        if (normalized.isEmpty()) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
        }
        Long count = authMethodMapper.selectCount(new LambdaQueryWrapper<AuthMethod>()
                .in(AuthMethod::getCode, normalized)
                .eq(AuthMethod::getStatus, AuthMethodStatus.ENABLED.getCode()));
        if (count == null || count != normalized.size()) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_DISABLED);
        }
    }

    /**
     * 将列表查询的逗号分隔字段转换为集合。
     *
     * @param responses 列表响应
     */
    public void enrichListResponses(List<AuthMethodResponse> responses) {
        responses.forEach(response -> {
            response.setPositions(split(response.getPositionsText()));
            response.setParams(maskSensitiveParams(readParams(response.getParamsJson())));
        });
    }

    /**
     * 转换启用认证方式选择项中的可用位置。
     *
     * @param responses 选择项响应
     */
    public void enrichOptionResponses(List<AuthMethodOptionResponse> responses) {
        responses.forEach(response -> response.setPositions(split(response.getPositionsText())));
    }

    /**
     * 组装详情响应并对敏感参数脱敏。
     *
     * @param method 认证方式实体
     * @return 认证方式详情
     */
    public AuthMethodDetailResponse buildInfoResponse(AuthMethod method) {
        AuthMethodDetailResponse response = new AuthMethodDetailResponse();
        BeanUtils.copyProperties(method, response, "positions", "paramsJson");
        response.setPositions(split(method.getPositions()));
        response.setParams(maskSensitiveParams(readParams(method.getParamsJson())));
        List<AuthMethodReferenceResponse> references = authMethodMapper.listReferences(method.getCode());
        response.setReferences(references);
        response.setReferenceCount(references.size());
        return response;
    }

    /**
     * 将请求参数转换为实体，创建场景使用。
     *
     * @param request 保存请求
     * @return 认证方式实体
     */
    public AuthMethod buildModel(AuthMethodRequest request) {
        AuthMethod method = new AuthMethod();
        copyRequest(request, method);
        method.setSystemBuiltin(AuthMethodTemplate.findByCode(request.getCode()).isPresent());
        method.setDeleted(Boolean.FALSE);
        return method;
    }

    /**
     * 将编辑请求覆盖到已有实体。编码保存后保持不变，掩码参数不会覆盖原始敏感值。
     *
     * @param request 保存请求
     * @param method 已有认证方式实体
     */
    public void copyForUpdate(AuthMethodRequest request, AuthMethod method) {
        if (!Objects.equals(method.getCode(), request.getCode())) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_CODE_IMMUTABLE);
        }
        Map<String, Object> params = mergeMaskedParams(readParams(method.getParamsJson()), request.getParams());
        request.setParams(params);
        copyRequest(request, method);
    }

    private void copyRequest(AuthMethodRequest request, AuthMethod method) {
        method.setCode(request.getCode());
        method.setName(request.getName());
        method.setPositions(String.join(",", request.getPositions()));
        method.setApplicableRange(request.getApplicableRange());
        method.setParamsJson(writeParams(request.getParams()));
        method.setSortNo(request.getSortNo());
        method.setStatus(request.getStatus());
        method.setDescription(request.getDescription());
    }

    private Map<String, Object> normalizeAndValidateParams(String code, Map<String, Object> params) {
        Map<String, Object> submitted = new LinkedHashMap<>(params == null ? Collections.emptyMap() : params);
        Optional<AuthMethodTemplate> template = AuthMethodTemplate.findByCode(code);
        if (template.isPresent()) {
            return normalizePresetTemplateParams(template.get(), submitted);
        }
        if (!submitted.containsKey(FORM_SCHEMA_KEY) && matchesSupportedTemplateParams(submitted)) {
            return submitted;
        }
        validateFormSchema(submitted.get(FORM_SCHEMA_KEY));
        return Map.of(FORM_SCHEMA_KEY, submitted.get(FORM_SCHEMA_KEY));
    }

    private Map<String, Object> normalizePresetTemplateParams(AuthMethodTemplate template, Map<String, Object> params) {
        if (params.containsKey(FORM_SCHEMA_KEY)) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_TEMPLATE_LOCKED);
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!template.getAllowedParamKeys().contains(entry.getKey())) {
                throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
            }
            normalized.put(entry.getKey(), entry.getValue());
        }
        return normalized;
    }

    private void validateFormSchema(Object formSchema) {
        if (!(formSchema instanceof List<?> fields) || fields.isEmpty()) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_FORM_SCHEMA_REQUIRED);
        }
        Set<String> fieldCodes = new java.util.HashSet<>();
        for (Object field : fields) {
            if (!(field instanceof Map<?, ?> fieldMap)) {
                throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
            }
            Object code = fieldMap.get(FIELD_CODE_KEY);
            if (!(code instanceof String fieldCode) || fieldCode.trim().isEmpty()) {
                throw new BizException(RealmErrorCode.AUTH_METHOD_FORM_SCHEMA_REQUIRED);
            }
            if (!fieldCodes.add(fieldCode.trim())) {
                throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
            }
        }
    }

    private boolean matchesSupportedTemplateParams(Map<String, Object> params) {
        if (params.isEmpty()) {
            return false;
        }
        Set<String> keys = params.keySet();
        return SUPPORTED_TEMPLATE_PARAM_KEYS.stream()
                .anyMatch(supportedKeys -> supportedKeys.containsAll(keys));
    }

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

    private List<String> split(String value) {
        return value == null || value.isBlank() ? Collections.emptyList() : List.of(value.split(","));
    }

    private String writeParams(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params == null ? Collections.emptyMap() : params);
        } catch (JsonProcessingException exception) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
        }
    }

    private Map<String, Object> readParams(String paramsJson) {
        if (paramsJson == null || paramsJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(paramsJson, new TypeReference<>() { });
        } catch (JsonProcessingException exception) {
            throw new BizException(RealmErrorCode.AUTH_METHOD_PARAM_ERROR);
        }
    }

    private Map<String, Object> maskSensitiveParams(Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>(params);
        result.replaceAll((key, value) -> isSensitiveKey(key) && value != null ? MASK_VALUE : value);
        return result;
    }

    private Map<String, Object> mergeMaskedParams(Map<String, Object> original, Map<String, Object> submitted) {
        Map<String, Object> result = new LinkedHashMap<>(submitted == null ? Collections.emptyMap() : submitted);
        result.replaceAll((key, value) ->
                isSensitiveKey(key) && MASK_VALUE.equals(value) ? original.get(key) : value);
        return result;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("secret") || normalized.contains("password")
                || normalized.contains("token") || normalized.contains("privatekey");
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private int value(Integer count) {
        return count == null ? 0 : count;
    }


    /**
     * 获取认证方式列表
     */
    public List<AuthMethod> findAuthMethods(List<Long> authMethodIds) {
        return authMethodMapper.selectByIds(authMethodIds);
    }
}
