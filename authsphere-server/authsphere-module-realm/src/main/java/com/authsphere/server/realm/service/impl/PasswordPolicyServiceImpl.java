package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.PasswordPolicyConvert;
import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyPageRequest;
import com.authsphere.server.realm.dto.PasswordPolicyRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.PasswordPolicyMapper;
import com.authsphere.server.realm.model.PasswordPolicy;
import com.authsphere.server.realm.service.PasswordPolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 密码策略模板服务实现。
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private final PasswordPolicyMapper passwordPolicyMapper;

    @Override
    public Page<PasswordPolicyListResponse> page(PasswordPolicyPageRequest request) {
        Page<PasswordPolicyListResponse> page = new Page<>(request.getPage(), request.getSize());
        return passwordPolicyMapper.page(page, request);
    }

    @Override
    public PasswordPolicyResponse getById(Long id) {
        return PasswordPolicyConvert.INSTANCE.response(findPolicyById(id));
    }

    /**
     * 保存密码规则
     *
     */
    @Override
    public PasswordPolicyResponse create(PasswordPolicyRequest request) {
        checkLengthRange(request);
        checkCodeExists(null, request.getCode());

        PasswordPolicy passwordPolicy = PasswordPolicyConvert.INSTANCE.model(request);
        passwordPolicy.setStatus(StatusEnum.NORMAL.getCode());
        passwordPolicyMapper.insert(passwordPolicy);
        return PasswordPolicyConvert.INSTANCE.response(passwordPolicy);
    }

    @Override
    public PasswordPolicyResponse update(Long id, PasswordPolicyRequest request) {
        checkLengthRange(request);
        PasswordPolicy passwordPolicy = findPolicyById(id);
        checkCodeExists(id, request.getCode());

        PasswordPolicyConvert.INSTANCE.copyToModel(request, passwordPolicy);
        passwordPolicyMapper.updateById(passwordPolicy);
        return PasswordPolicyConvert.INSTANCE.response(passwordPolicy);
    }

    /**
     * 修改密码规则状态
     *
     */
    @Override
    public Boolean editStatus(Long id) {
        PasswordPolicy passwordPolicy = findPolicyById(id);
        Integer status = StatusEnum.NORMAL.getCode().equals(passwordPolicy.getStatus())
                ? StatusEnum.DISABLED.getCode()
                : StatusEnum.NORMAL.getCode();
        passwordPolicy.setStatus(status);
        passwordPolicyMapper.updateById(passwordPolicy);
        return Boolean.TRUE;
    }

    /**
     * 获取所有的密码策略
     */
    @Override
    public List<PasswordPolicyListResponse> list() {

        List<PasswordPolicy> passwordPolicies = passwordPolicyMapper
                .selectList(new LambdaQueryWrapper<PasswordPolicy>().eq(PasswordPolicy::getStatus, StatusEnum.NORMAL.getCode()));
        if (CollectionUtils.isEmpty(passwordPolicies)) {
            return Collections.emptyList();
        }
        return PasswordPolicyConvert.INSTANCE.toPasswordPolicyListResponse(passwordPolicies);
    }

    private PasswordPolicy findPolicyById(Long id) {
        PasswordPolicy passwordPolicy = passwordPolicyMapper.selectById(id);
        if (passwordPolicy == null) {
            throw new BizException(RealmErrorCode.PASSWORD_POLICY_DATA_ERROR);
        }
        return passwordPolicy;
    }


    /**
     * 校验 code 是否存在
     */
    private void checkCodeExists(Long currentId, String code) {
        LambdaQueryWrapper<PasswordPolicy> wrapper = new LambdaQueryWrapper<PasswordPolicy>().eq(PasswordPolicy::getCode, code);
        if (currentId != null) {
            wrapper.ne(PasswordPolicy::getId, currentId);
        }

        if (passwordPolicyMapper.selectCount(wrapper) > 0) {
            throw new BizException(RealmErrorCode.PASSWORD_POLICY_CODE_EXISTS);
        }
    }

    /**
     * 校验密码长度范围
     */
    private void checkLengthRange(PasswordPolicyRequest request) {
        if (request.getMinLength() > request.getMaxLength()) {
            throw new BizException(RealmErrorCode.PASSWORD_POLICY_PARAM_ERROR);
        }
    }

}
