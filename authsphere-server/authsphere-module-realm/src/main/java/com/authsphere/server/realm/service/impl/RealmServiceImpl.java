package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.dto.AuthMethodInfoResponse;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmDetailResponse;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.RealmType;
import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.mapper.RealmAuthMethodRelMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.authsphere.server.realm.service.RealmService;
import com.authsphere.server.realm.enums.SsoSingleLogoutEnum;
import com.authsphere.server.realm.enums.ExistingSessionHandlerEnum;
import com.authsphere.server.realm.enums.NoClientIdHandlerEnum;
import com.authsphere.server.realm.model.RealmAuthMethodRel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.authsphere.server.common.enums.StatusEnum.DISABLED;
import static com.authsphere.server.common.enums.StatusEnum.NORMAL;
import static com.authsphere.server.realm.error.RealmErrorCode.REALM_DATA_ERROR;

/**
 * @author longjiangran
 * @description 针对表【realm(身份域信息)】的数据库操作Service实现
 * @createDate 2026-05-26 14:40:36
 */
@Service
@RequiredArgsConstructor
public class RealmServiceImpl implements RealmService {
    private final RealmMapper realmMapper;
    private final RealmDomain realmDomain;
    private final RealmTypeDomain realmTypeDomain;
    private final AuthMethodMapper authMethodMapper;
    private final RealmAuthMethodRelMapper realmAuthMethodRelMapper;

    /**
     * 创建身份域。
     *
     * <p>创建时除了保存 realm 主表，还会同步写入身份域与认证方式的关系表，
     * 保证“支持的认证方式列表”和“默认认证方式/MFA 方式”属于同一份配置快照。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(CreateRealmRequest createRealmRequest) {
        List<Realm> realms = realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .eq(Realm::getCode, createRealmRequest.getCode()));
        if (!CollectionUtils.isEmpty(realms)) {
            throw new BizException(RealmErrorCode.REALM_CODE_EXISTS);
        }
        validateRealmType(createRealmRequest.getRealmTypeId());
        fillSsoDefaults(createRealmRequest);
        fillSecurityDefaults(createRealmRequest);
        validateSsoConfig(createRealmRequest);
        Map<Long, AuthMethod> authMethodMap = loadAuthMethods(createRealmRequest.getAuthMethodIds());
        validateAuthConfig(createRealmRequest, authMethodMap);
        Realm realm = RealmConvert.INSTANCE.model(createRealmRequest);
        realm.setStatus(createRealmRequest.getStatus() == null ? NORMAL.getCode() : createRealmRequest.getStatus());
        boolean saved = realmMapper.insert(realm) > 0;
        saveAuthMethodRelations(realm.getId(), createRealmRequest.getAuthMethodIds());
        return saved;
    }

    /**
     * 更新身份域。
     *
     * <p>更新时直接覆盖身份域聚合字段，并重建认证方式关系表，
     * 不做局部增删，避免前后端状态漂移。</p>
     *
     * @param id 身份域 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, CreateRealmRequest createRealmRequest) {
        Realm realm = findById(id);
        validateRealmType(createRealmRequest.getRealmTypeId());
        fillSsoDefaults(createRealmRequest);
        fillSecurityDefaults(createRealmRequest);
        validateSsoConfig(createRealmRequest);
        Map<Long, AuthMethod> authMethodMap = loadAuthMethods(createRealmRequest.getAuthMethodIds());
        validateAuthConfig(createRealmRequest, authMethodMap);
        RealmConvert.INSTANCE.copyByModel(createRealmRequest, realm);
        realm.setStatus(createRealmRequest.getStatus() == null ? realm.getStatus() : createRealmRequest.getStatus());
        realmMapper.updateById(realm);
        replaceAuthMethodRelations(id, createRealmRequest.getAuthMethodIds());
        return Boolean.TRUE;
    }

    /**
     * 修改身份域状态。
     *
     * <p>当前实现保持最小语义，只做启用/禁用取反。</p>
     *
     * @param id 身份域 id
     */
    @Override
    public Boolean editStatus(Long id) {
        Realm realm = findById(id);
        Integer status = realm.getStatus().equals(NORMAL.getCode()) ? DISABLED.getCode() : NORMAL.getCode();
        realm.setStatus(status);
        realmMapper.updateById(realm);
        return Boolean.TRUE;
    }

    /**
     * 查询身份域分页列表。
     *
     * <p>分页 SQL 负责取基础信息和统计字段，
     * Service 层补齐身份域类型名称、默认认证方式名称和认证方式列表。</p>
     */
    @Override
    public Page<RealmPageResponse> page(RealmPageRequest realmPageRequest) {
        Page<RealmPageResponse> page = new Page<>(realmPageRequest.getPage(), realmPageRequest.getSize());
        Page<RealmPageResponse> result = realmMapper.page(page, realmPageRequest);
        List<RealmPageResponse> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        List<Long> realmTypeIdList = records.stream().map(RealmPageResponse::getRealmTypeId).toList();
        List<RealmType> realmTypeList = realmTypeDomain.findByIdList(realmTypeIdList);
        Map<Long, RealmType> realmTypeMap = realmTypeList.stream().collect(Collectors.toMap(RealmType::getId, e -> e));
        Map<Long, AuthMethod> authMethodMap = loadAuthMethodsByRealmRecords(records);
        Map<Long, List<AuthMethodInfoResponse>> realmAuthMethodMap = buildRealmAuthMethodMap(records, authMethodMap);

        records.forEach(e -> {
            RealmType realmType = realmTypeMap.get(e.getRealmTypeId());
            e.setRealmTypeName(realmType == null ? null : realmType.getName());
            e.setAuthMethodList(realmAuthMethodMap.getOrDefault(e.getId(), Collections.emptyList()));
            AuthMethod defaultAuthMethod = authMethodMap.get(e.getDefaultAuthMethodId());
            e.setDefaultAuthMethodName(defaultAuthMethod == null ? null : defaultAuthMethod.getName());
        });


        return result;
    }

    /**
     * 查询身份域详情。
     *
     * <p>详情页复用 realm 主表数据，再补充认证方式关系、默认认证方式名称和引用统计。</p>
     */
    @Override
    public RealmDetailResponse detail(Long id) {
        Realm realm = findById(id);
        RealmDetailResponse response = new RealmDetailResponse();
        BeanUtils.copyProperties(realm, response);
        response.setAuthMethodIds(listAuthMethodIds(id));

        List<RealmPageResponse> records = new ArrayList<>();
        RealmPageResponse pageResponse = new RealmPageResponse();
        BeanUtils.copyProperties(realm, pageResponse);
        pageResponse.setId(realm.getId());
        records.add(pageResponse);
        Map<Long, AuthMethod> authMethodMap = loadAuthMethodsByRealmRecords(records);
        Map<Long, List<AuthMethodInfoResponse>> realmAuthMethodMap = buildRealmAuthMethodMap(records, authMethodMap);
        response.setAuthMethodList(realmAuthMethodMap.getOrDefault(id, Collections.emptyList()));
        AuthMethod defaultAuthMethod = authMethodMap.get(response.getDefaultAuthMethodId());
        response.setDefaultAuthMethodName(defaultAuthMethod == null ? null : defaultAuthMethod.getName());

        List<RealmType> realmTypes = realmTypeDomain.findByIdList(List.of(realm.getRealmTypeId()));
        if (!CollectionUtils.isEmpty(realmTypes)) {
            response.setRealmTypeName(realmTypes.getFirst().getName());
        }
        response.setSsoClientCount((long) realmMapper.countClientReferences(id));
        response.setAccountCount((long) realmMapper.countAccountReferences(id));
        return response;
    }


    /**
     * 校验 SSO 配置合法性。
     *
     * <p>这里只做枚举值边界校验，不在此处处理“开关关闭时是否清空字段”的策略。</p>
     */
    private void validateSsoConfig(CreateRealmRequest request) {
        if (StringUtils.hasText(request.getSsoSingleLogout()) && !SsoSingleLogoutEnum.isValid(request.getSsoSingleLogout())) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR.getCode(), "单点退出策略值无效");
        }
        if (StringUtils.hasText(request.getExistingSessionHandler()) && !ExistingSessionHandlerEnum.isValid(request.getExistingSessionHandler())) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR.getCode(), "已存在会话处理方式值无效");
        }
        if (StringUtils.hasText(request.getNoClientIdHandler()) && !NoClientIdHandlerEnum.isValid(request.getNoClientIdHandler())) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR.getCode(), "无 client_id 时的处理方式值无效");
        }
    }

    /**
     * 填充 SSO 默认值。
     *
     * <p>即使前端未传完整字段，后端也保证落库值稳定，避免出现 null 语义漂移。</p>
     */
    private void fillSsoDefaults(CreateRealmRequest request) {
        if (request.getSsoEnabled() == null) {
            request.setSsoEnabled(Boolean.TRUE);
        }
        if (request.getSsoSessionTimeout() == null) {
            request.setSsoSessionTimeout(8);
        }
        if (request.getSsoIdleTimeout() == null) {
            request.setSsoIdleTimeout(30);
        }
        if (!StringUtils.hasText(request.getSsoSingleLogout())) {
            request.setSsoSingleLogout(SsoSingleLogoutEnum.ENABLED.getCode());
        }
        if (!StringUtils.hasText(request.getExistingSessionHandler())) {
            request.setExistingSessionHandler(ExistingSessionHandlerEnum.AUTO_REDIRECT.getCode());
        }
        if (!StringUtils.hasText(request.getNoClientIdHandler())) {
            request.setNoClientIdHandler(NoClientIdHandlerEnum.SHOW_APP_LIST.getCode());
        }
    }

    /**
     * 填充认证与安全配置默认值。
     *
     * <p>这批字段改为身份域直接保存后，默认值必须由后端兜住，
     * 否则不同页面、不同版本前端可能写出不一致数据。</p>
     */
    private void fillSecurityDefaults(CreateRealmRequest request) {
        //填充图形验证码默认值
        if (ObjectUtils.isEmpty(request.getCaptchaMode())) {
            request.setCaptchaMode("none");

        }
        fillPasswordDefaults(request);
        fillTokenDefaults(request);
        fillSessionDefaults(request);
        fillAccountSecurityDefaults(request);
    }


    /**
     * 填充密码安全默认值。
     */
    private void fillPasswordDefaults(CreateRealmRequest request) {
        if (ObjectUtils.isEmpty(request.getPasswordMinLength())) {
            request.setPasswordMinLength(8);
        }
        if (ObjectUtils.isEmpty(request.getPasswordMaxLength())) {
            request.setPasswordMaxLength(32);
        }
        if (!StringUtils.hasText(request.getPasswordComplexity())) {
            request.setPasswordComplexity("letters_digits");
        }
        if (ObjectUtils.isEmpty(request.getPasswordExpireDays())) {
            request.setPasswordExpireDays(90);
        }
    }

    /**
     * 填充 Token 安全默认值。
     */
    private void fillTokenDefaults(CreateRealmRequest request) {
        if (request.getAccessTokenTimeout() == null) {
            request.setAccessTokenTimeout(120);
        }
        if (request.getRefreshTokenTimeout() == null) {
            request.setRefreshTokenTimeout(7);
        }
        if (request.getTokenRotationEnabled() == null) {
            request.setTokenRotationEnabled(Boolean.TRUE);
        }
        if (request.getTokenBlacklistEnabled() == null) {
            request.setTokenBlacklistEnabled(Boolean.TRUE);
        }
    }

    /**
     * 填充会话安全默认值。
     */
    private void fillSessionDefaults(CreateRealmRequest request) {
        if (request.getSessionIdleTimeout() == null) {
            request.setSessionIdleTimeout(30);
        }
        if (!StringUtils.hasText(request.getSessionMultiDevice())) {
            request.setSessionMultiDevice("allow");
        }
        if (request.getSessionMaxDevices() == null) {
            request.setSessionMaxDevices(5);
        }
    }

    /**
     * 填充账号安全默认值。
     */
    private void fillAccountSecurityDefaults(CreateRealmRequest request) {
        if (ObjectUtils.isEmpty(request.getLoginFailMaxCount())) {
            request.setLoginFailMaxCount(5);
        }
        if (ObjectUtils.isEmpty(request.getLoginFailWindowMinutes())) {
            request.setLoginFailWindowMinutes(10);
        }
        if (ObjectUtils.isEmpty(request.getLoginFailLockMinutes())) {
            request.setLoginFailLockMinutes(30);
        }
        if (ObjectUtils.isEmpty(request.getLoginFailAutoUnlock())) {
            request.setLoginFailAutoUnlock(Boolean.TRUE);
        }
    }

    /**
     * 校验身份域类型是否存在。
     */
    private void validateRealmType(Long realmTypeId) {
        if (realmTypeId == null || CollectionUtils.isEmpty(realmTypeDomain.findByIdList(List.of(realmTypeId)))) {
            throw new BizException(REALM_DATA_ERROR.getCode(), "身份域类型不存在");
        }
    }

    /**
     * 按认证方式 ID 批量加载认证方式。
     *
     * <p>这里顺便完成“至少选择一个认证方式”和“认证方式必须真实存在”的校验。</p>
     */
    private Map<Long, AuthMethod> loadAuthMethods(List<Long> authMethodIds) {
        if (CollectionUtils.isEmpty(authMethodIds)) {
            throw new BizException("至少选择一个认证方式");
        }
        List<Long> distinctIds = authMethodIds.stream().filter(Objects::nonNull).distinct().toList();
        List<AuthMethod> methods = authMethodMapper.selectBatchIds(distinctIds);
        if (methods.size() != distinctIds.size()) {
            throw new BizException("存在无效的认证方式");
        }
        return methods.stream().collect(Collectors.toMap(AuthMethod::getId, method -> method));
    }

    /**
     * 校验认证配置。
     *
     * <p>核心约束：
     * 默认认证方式不能为空，且必须属于已选认证方式；
     * MFA 认证方式若存在，也必须属于已选认证方式；
     * threshold 验证码模式必须配置有效阈值。</p>
     */
    private void validateAuthConfig(CreateRealmRequest request, Map<Long, AuthMethod> authMethodMap) {
        if (request.getDefaultAuthMethodId() == null) {
            throw new BizException("默认认证方式不能为空");
        }
        if (!request.getAuthMethodIds().contains(request.getDefaultAuthMethodId())) {
            throw new BizException("默认认证方式必须属于已选认证方式");
        }
        if (!authMethodMap.containsKey(request.getDefaultAuthMethodId())) {
            throw new BizException("默认认证方式不存在");
        }
        if (request.getMfaAuthMethodId() != null && !authMethodMap.containsKey(request.getMfaAuthMethodId())) {
            throw new BizException("MFA认证方式必须属于已选认证方式");
        }
        if ("threshold".equals(request.getCaptchaMode())
                && (request.getCaptchaThreshold() == null || request.getCaptchaThreshold() <= 0)) {
            throw new BizException("图形验证码阈值必须大于0");
        }
    }

    /**
     * 首次保存身份域与认证方式的关系。
     */
    private void saveAuthMethodRelations(Long realmId, List<Long> authMethodIds) {
        for (Long authMethodId : authMethodIds.stream().filter(Objects::nonNull).distinct().toList()) {
            RealmAuthMethodRel relation = new RealmAuthMethodRel();
            relation.setRealmId(realmId);
            relation.setAuthMethodId(authMethodId);
            realmAuthMethodRelMapper.insert(relation);
        }
    }

    /**
     * 重建身份域与认证方式的关系。
     *
     * <p>更新场景直接删旧建新，逻辑最短，也能避免脏关联残留。</p>
     */
    private void replaceAuthMethodRelations(Long realmId, List<Long> authMethodIds) {
        realmAuthMethodRelMapper.delete(new LambdaQueryWrapper<RealmAuthMethodRel>()
                .eq(RealmAuthMethodRel::getRealmId, realmId));
        saveAuthMethodRelations(realmId, authMethodIds);
    }

    /**
     * 查询身份域当前绑定的认证方式 ID 列表。
     */
    private List<Long> listAuthMethodIds(Long realmId) {
        return realmAuthMethodRelMapper.selectList(new LambdaQueryWrapper<RealmAuthMethodRel>()
                        .eq(RealmAuthMethodRel::getRealmId, realmId))
                .stream()
                .map(RealmAuthMethodRel::getAuthMethodId)
                .toList();
    }

    /**
     * 为一批身份域记录加载所有需要的认证方式实体。
     *
     * <p>同时覆盖两类数据来源：
     * 1. 身份域已绑定的认证方式；
     * 2. 默认认证方式字段。</p>
     */
    private Map<Long, AuthMethod> loadAuthMethodsByRealmRecords(List<RealmPageResponse> records) {
        List<Long> realmIds = records.stream().map(RealmPageResponse::getId).toList();
        List<RealmAuthMethodRel> relations = realmAuthMethodRelMapper.selectList(new LambdaQueryWrapper<RealmAuthMethodRel>()
                .in(RealmAuthMethodRel::getRealmId, realmIds));
        List<Long> authMethodIds = relations.stream().map(RealmAuthMethodRel::getAuthMethodId).distinct().collect(Collectors.toCollection(ArrayList::new));
        records.stream()
                .map(RealmPageResponse::getDefaultAuthMethodId)
                .filter(Objects::nonNull)
                .filter(id -> !authMethodIds.contains(id))
                .forEach(authMethodIds::add);
        if (CollectionUtils.isEmpty(authMethodIds)) {
            return Collections.emptyMap();
        }
        return authMethodMapper.selectBatchIds(authMethodIds).stream()
                .collect(Collectors.toMap(AuthMethod::getId, method -> method));
    }

    /**
     * 构造“身份域 -> 认证方式摘要列表”的聚合结果。
     */
    private Map<Long, List<AuthMethodInfoResponse>> buildRealmAuthMethodMap(List<RealmPageResponse> records,
                                                                            Map<Long, AuthMethod> authMethodMap) {
        List<Long> realmIds = records.stream().map(RealmPageResponse::getId).toList();
        List<RealmAuthMethodRel> relations = realmAuthMethodRelMapper.selectList(new LambdaQueryWrapper<RealmAuthMethodRel>()
                .in(RealmAuthMethodRel::getRealmId, realmIds));
        return relations.stream().collect(Collectors.groupingBy(RealmAuthMethodRel::getRealmId,
                Collectors.mapping(relation -> toAuthMethodInfo(authMethodMap.get(relation.getAuthMethodId())),
                        Collectors.filtering(Objects::nonNull, Collectors.toList()))));
    }

    /**
     * 把认证方式实体压平成列表页/详情页需要的摘要对象。
     */
    private AuthMethodInfoResponse toAuthMethodInfo(AuthMethod method) {
        if (method == null) {
            return null;
        }
        AuthMethodInfoResponse info = new AuthMethodInfoResponse();
        info.setId(method.getId());
        info.setCode(method.getCode());
        info.setName(method.getName());
        info.setDescription(method.getDescription());
        return info;
    }


    public Realm findById(Long id) {
        Realm realm = realmMapper.selectById(id);

        if (realm == null) {
            throw new BizException(REALM_DATA_ERROR);
        }

        return realm;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Realm realm = findById(id);
        if (realmMapper.countAccountReferences(id) > 0) {
            throw new BizException("该身份域下存在关联的账号，无法删除");
        }
        if (realmMapper.countClientReferences(id) > 0) {
            throw new BizException("该身份域下存在关联的客户端，无法删除");
        }
        realmMapper.deleteById(id);
        return Boolean.TRUE;
    }

    @Override
    public List<RealmListResponse> list() {
        return realmMapper.listAll();
    }
}
