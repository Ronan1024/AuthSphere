package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.UniquePolicyConvert;
import com.authsphere.server.realm.dto.UniquePolicyPageRequest;
import com.authsphere.server.realm.dto.UniquePolicyRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.UniquePolicyMapper;
import com.authsphere.server.realm.model.UniquePolicy;
import com.authsphere.server.realm.service.UniquePolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 账号唯一性规则模板服务实现。
 */
@Service
@RequiredArgsConstructor
public class UniquePolicyServiceImpl implements UniquePolicyService {

    private final UniquePolicyMapper uniquePolicyMapper;

    @Override
    public Page<UniquePolicyResponse> page(UniquePolicyPageRequest request) {
        Page<UniquePolicyResponse> page = new Page<>(request.getPage(), request.getSize());
        return uniquePolicyMapper.page(page, request);
    }

    @Override
    public UniquePolicyResponse getById(Long id) {
        return UniquePolicyConvert.INSTANCE.response(findPolicyById(id));
    }

    @Override
    public UniquePolicyResponse create(UniquePolicyRequest request) {
        checkCodeExists(null, request.getCode());
        UniquePolicy policy = UniquePolicyConvert.INSTANCE.model(request);
        policy.setStatus(StatusEnum.NORMAL.getCode());
        uniquePolicyMapper.insert(policy);
        return UniquePolicyConvert.INSTANCE.response(policy);
    }

    @Override
    public UniquePolicyResponse update(Long id, UniquePolicyRequest request) {
        UniquePolicy policy = findPolicyById(id);
        checkCodeExists(id, request.getCode());
        UniquePolicyConvert.INSTANCE.copyToModel(request, policy);
        uniquePolicyMapper.updateById(policy);
        return UniquePolicyConvert.INSTANCE.response(policy);
    }

    @Override
    public Boolean editStatus(Long id) {
        UniquePolicy policy = findPolicyById(id);
        Integer status = StatusEnum.NORMAL.getCode().equals(policy.getStatus())
                ? StatusEnum.DISABLED.getCode()
                : StatusEnum.NORMAL.getCode();
        policy.setStatus(status);
        uniquePolicyMapper.updateById(policy);
        return Boolean.TRUE;
    }

    private UniquePolicy findPolicyById(Long id) {
        UniquePolicy policy = uniquePolicyMapper.selectById(id);
        if (policy == null) {
            throw new BizException(RealmErrorCode.UNIQUE_POLICY_DATA_ERROR);
        }
        return policy;
    }

    private void checkCodeExists(Long currentId, String code) {
        LambdaQueryWrapper<UniquePolicy> wrapper = new LambdaQueryWrapper<UniquePolicy>()
                .eq(UniquePolicy::getCode, code);
        if (currentId != null) {
            wrapper.ne(UniquePolicy::getId, currentId);
        }
        if (uniquePolicyMapper.selectCount(wrapper) > 0) {
            throw new BizException(RealmErrorCode.UNIQUE_POLICY_CODE_EXISTS);
        }
    }
}
