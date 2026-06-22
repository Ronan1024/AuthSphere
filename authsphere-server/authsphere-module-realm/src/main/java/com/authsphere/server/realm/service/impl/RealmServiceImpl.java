package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.domain.AuthPolicyDomain;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.dto.AuthMethodInfoResponse;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.mapper.AuthPolicyMapper;
import com.authsphere.server.realm.model.AuthPolicy;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.RealmType;
import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.authsphere.server.realm.service.AuthPolicyService;
import com.authsphere.server.realm.service.RealmService;
import com.authsphere.server.realm.enums.SsoSingleLogoutEnum;
import com.authsphere.server.realm.enums.ExistingSessionHandlerEnum;
import com.authsphere.server.realm.enums.NoClientIdHandlerEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
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
    private final AuthPolicyMapper authPolicyMapper;
    private final AuthPolicyService authPolicyService;
    private final AuthPolicyDomain authPolicyDomain;

    private final AuthMethodMapper authMethodMapper;

    /**
     * 创建身份域信息
     *
     */
    @Override
    public Boolean create(CreateRealmRequest createRealmRequest) {
        List<Realm> realms = realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .eq(Realm::getCode, createRealmRequest.getCode()));
        if (!CollectionUtils.isEmpty(realms)) {
            throw new BizException(RealmErrorCode.REALM_CODE_EXISTS);
        }
        authPolicyDomain.checkEnabled(createRealmRequest.getAuthPolicyId());
        fillSsoDefaults(createRealmRequest);
        validateSsoConfig(createRealmRequest);
        Realm realm = RealmConvert.INSTANCE.model(createRealmRequest);
        realm.setStatus(NORMAL.getCode());
        return realmMapper.insert(realm) > 0;
    }

    /**
     * 更新身份域信息
     *
     * @param id 身份域id
     */
    @Override
    public Boolean update(Long id, CreateRealmRequest createRealmRequest) {
        Realm realm = findById(id);
        authPolicyDomain.checkEnabled(createRealmRequest.getAuthPolicyId());
        fillSsoDefaults(createRealmRequest);
        validateSsoConfig(createRealmRequest);
        RealmConvert.INSTANCE.copyByModel(createRealmRequest, realm);
        realmMapper.updateById(realm);
        return Boolean.TRUE;
    }

    /**
     * 修改身份域状态 修改当前状态的取反状态
     *
     * @param id 状态域id
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
     * 身份域分页列表
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
        List<Long> authPolicyIds = records.stream()
                .map(RealmPageResponse::getAuthPolicyId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, AuthPolicy> authPolicyMap = CollectionUtils.isEmpty(authPolicyIds)
                ? java.util.Collections.emptyMap()
                : authPolicyMapper.selectBatchIds(authPolicyIds).stream()
                .collect(Collectors.toMap(AuthPolicy::getId, e -> e));

        List<AuthMethod> allAuthMethods = authMethodMapper.selectList(new LambdaQueryWrapper<AuthMethod>()
                .eq(AuthMethod::getStatus, NORMAL.getCode()));
        Map<String, AuthMethod> authMethodMap = allAuthMethods.stream()
                .collect(Collectors.toMap(AuthMethod::getCode, e -> e, (v1, v2) -> v1));

        records.forEach(e -> {
            RealmType realmType = realmTypeMap.get(e.getRealmTypeId());
            e.setRealmTypeName(realmType == null ? null : realmType.getName());
            AuthPolicy authPolicy = authPolicyMap.get(e.getAuthPolicyId());
            e.setAuthPolicyName(authPolicy == null ? null : authPolicy.getName());

            if (authPolicy != null && authPolicy.getAuthMethods() != null) {
                List<AuthMethodInfoResponse> methodList = Arrays.stream(authPolicy.getAuthMethods().split(","))
                        .map(String::trim)
                        .filter(code -> !code.isEmpty())
                        .map(authMethodMap::get)
                        .filter(Objects::nonNull)
                        .map(method -> {
                            AuthMethodInfoResponse info = new AuthMethodInfoResponse();
                            info.setId(method.getId());
                            info.setCode(method.getCode());
                            info.setName(method.getName());
                            info.setDescription(method.getDescription());
                            return info;
                        })
                        .collect(Collectors.toList());
                e.setAuthMethodList(methodList);
            } else {
                e.setAuthMethodList(java.util.Collections.emptyList());
            }
        });


        return result;
    }


    /**
     * 校验 SSO 配置合法性
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
     * 填充默认值
     *
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


