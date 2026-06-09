package com.authsphere.server.realm.service.impl;

import com.authsphere.server.realm.domain.AuthMethodDomain;
import com.authsphere.server.realm.dto.AuthMethodDetailResponse;
import com.authsphere.server.realm.dto.AuthMethodOptionResponse;
import com.authsphere.server.realm.dto.AuthMethodPageRequest;
import com.authsphere.server.realm.dto.AuthMethodRequest;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.enums.AuthMethodStatus;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.authsphere.server.realm.service.AuthMethodService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证方式管理服务实现，负责认证方式管理用例编排。
 */
@Service
@RequiredArgsConstructor
public class AuthMethodServiceImpl implements AuthMethodService {

    private final AuthMethodMapper authMethodMapper;
    private final AuthMethodDomain authMethodDomain;

    /**
     * 分页查询认证方式，并补充可用位置集合。
     *
     * @param request 分页及筛选条件
     * @return 认证方式分页数据
     */
    @Override
    public Page<AuthMethodResponse> page(AuthMethodPageRequest request) {
        Page<AuthMethodResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<AuthMethodResponse> result = authMethodMapper.page(page, request);
        authMethodDomain.enrichListResponses(result.getRecords());
        return result;
    }

    /**
     * 查询启用认证方式选择项，可按可用位置筛选。
     *
     * @param position 可用位置；为空表示查询全部
     * @return 启用认证方式选择项
     */
    @Override
    public List<AuthMethodOptionResponse> listEnabled(String position) {
        List<AuthMethodOptionResponse> responses = authMethodMapper.listEnabled(position);
        authMethodDomain.enrichOptionResponses(responses);
        return responses;
    }

    /**
     * 查询认证方式详情、脱敏参数和引用策略。
     *
     * @param id 认证方式主键
     * @return 认证方式详情
     */
    @Override
    public AuthMethodDetailResponse detail(Long id) {
        return authMethodDomain.buildInfoResponse(authMethodDomain.findById(id));
    }

    /**
     * 新增认证方式。
     *
     * @param request 保存请求
     * @return 新增认证方式主键
     */
    @Override
    public Long create(AuthMethodRequest request) {
        authMethodDomain.normalizeAndValidate(request);
        authMethodDomain.checkCodeAvailable(null, request.getCode());
        AuthMethod method = authMethodDomain.buildModel(request);
        authMethodMapper.insert(method);
        return method.getId();
    }

    /**
     * 编辑认证方式。认证方式编码和系统内置属性不可修改。
     *
     * @param id 认证方式主键
     * @param request 保存请求
     * @return 是否编辑成功
     */
    @Override
    public Boolean update(Long id, AuthMethodRequest request) {
        authMethodDomain.normalizeAndValidate(request);
        AuthMethod method = authMethodDomain.findById(id);
        authMethodDomain.copyForUpdate(request, method);
        authMethodMapper.updateById(method);
        return Boolean.TRUE;
    }

    /**
     * 启用认证方式。
     *
     * @param id 认证方式主键
     * @return 是否启用成功
     */
    @Override
    public Boolean enable(Long id) {
        AuthMethod method = authMethodDomain.findById(id);
        method.setStatus(AuthMethodStatus.ENABLED.getCode());
        authMethodMapper.updateById(method);
        return Boolean.TRUE;
    }

    /**
     * 禁用认证方式。已有策略引用保留，但新配置只能选择启用方式。
     *
     * @param id 认证方式主键
     * @return 是否禁用成功
     */
    @Override
    public Boolean disable(Long id) {
        AuthMethod method = authMethodDomain.findById(id);
        method.setStatus(AuthMethodStatus.DISABLED.getCode());
        authMethodMapper.updateById(method);
        return Boolean.TRUE;
    }

    /**
     * 删除非内置且未被认证策略引用的认证方式。
     *
     * @param id 认证方式主键
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(Long id) {
        AuthMethod method = authMethodDomain.findById(id);
        authMethodDomain.checkCanDelete(method);
        authMethodMapper.deleteById(id);
        return Boolean.TRUE;
    }

    /**
     * 校验认证方式编码均存在且处于启用状态。
     *
     * @param codes 认证方式编码集合
     */
    @Override
    public void validateEnabledCodes(List<String> codes) {
        authMethodDomain.checkEnabledCodes(codes);
    }
}
