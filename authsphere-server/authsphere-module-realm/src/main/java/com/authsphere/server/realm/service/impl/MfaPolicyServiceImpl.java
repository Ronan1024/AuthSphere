package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.MfaPolicyConvert;
import com.authsphere.server.realm.dto.MfaPolicyPageRequest;
import com.authsphere.server.realm.dto.MfaPolicyRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.MfaPolicyMapper;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.MfaPolicy;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.service.MfaPolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * MFA 策略模板服务实现。
 */
@Service
@RequiredArgsConstructor
public class MfaPolicyServiceImpl implements MfaPolicyService {

    private final MfaPolicyMapper mfaPolicyMapper;
    private final RealmMapper realmMapper;

    @Override
    public Page<MfaPolicyResponse> page(MfaPolicyPageRequest request) {
        Page<MfaPolicyResponse> page = new Page<>(request.getPage(), request.getSize());
        return mfaPolicyMapper.page(page, request);
    }

    @Override
    public MfaPolicyResponse getById(Long id) {
        return MfaPolicyConvert.INSTANCE.response(findPolicyById(id));
    }

    @Override
    public MfaPolicyResponse create(MfaPolicyRequest request) {
        checkCodeExists(null, request.getCode());
        MfaPolicy policy = MfaPolicyConvert.INSTANCE.model(request);
        policy.setStatus(StatusEnum.NORMAL.getCode());
        mfaPolicyMapper.insert(policy);
        return MfaPolicyConvert.INSTANCE.response(policy);
    }

    @Override
    public MfaPolicyResponse update(Long id, MfaPolicyRequest request) {
        MfaPolicy policy = findPolicyById(id);
        checkCodeExists(id, request.getCode());
        MfaPolicyConvert.INSTANCE.copyToModel(request, policy);
        mfaPolicyMapper.updateById(policy);
        return MfaPolicyConvert.INSTANCE.response(policy);
    }

    @Override
    public Boolean editStatus(Long id) {
        MfaPolicy policy = findPolicyById(id);
        Integer status = StatusEnum.NORMAL.getCode().equals(policy.getStatus())
                ? StatusEnum.DISABLED.getCode()
                : StatusEnum.NORMAL.getCode();
        policy.setStatus(status);
        mfaPolicyMapper.updateById(policy);
        return Boolean.TRUE;
    }

    @Override
    public MfaPolicyResponse getRealmPolicy(Long realmId) {
        Realm realm = findRealmById(realmId);
        if (realm.getMfaPolicy() == null) {
            throw new BizException(RealmErrorCode.MFA_POLICY_DATA_ERROR);
        }
        return getById(realm.getMfaPolicy());
    }

    @Override
    public Boolean bindRealmPolicy(Long realmId, Long policyId) {
        Realm realm = findRealmById(realmId);
        MfaPolicy policy = findPolicyById(policyId);
        if (!StatusEnum.NORMAL.getCode().equals(policy.getStatus())) {
            throw new BizException(RealmErrorCode.MFA_POLICY_DATA_ERROR);
        }
        realm.setMfaPolicy(policyId);
        realmMapper.updateById(realm);
        return Boolean.TRUE;
    }

    private Realm findRealmById(Long realmId) {
        Realm realm = realmMapper.selectById(realmId);
        if (realm == null) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR);
        }
        return realm;
    }

    private MfaPolicy findPolicyById(Long id) {
        MfaPolicy policy = mfaPolicyMapper.selectById(id);
        if (policy == null) {
            throw new BizException(RealmErrorCode.MFA_POLICY_DATA_ERROR);
        }
        return policy;
    }

    private void checkCodeExists(Long currentId, String code) {
        LambdaQueryWrapper<MfaPolicy> wrapper = new LambdaQueryWrapper<MfaPolicy>()
                .eq(MfaPolicy::getCode, code);
        if (currentId != null) {
            wrapper.ne(MfaPolicy::getId, currentId);
        }
        if (mfaPolicyMapper.selectCount(wrapper) > 0) {
            throw new BizException(RealmErrorCode.MFA_POLICY_CODE_EXISTS);
        }
    }
}
