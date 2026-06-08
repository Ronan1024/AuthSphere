package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.realm.convert.AuthPolicyConvert;
import com.authsphere.server.realm.domain.AuthPolicyDomain;
import com.authsphere.server.realm.dto.AuthPolicyCopyRequest;
import com.authsphere.server.realm.dto.AuthPolicyInfoResponse;
import com.authsphere.server.realm.dto.AuthPolicyOptionResponse;
import com.authsphere.server.realm.dto.AuthPolicyPageRequest;
import com.authsphere.server.realm.dto.AuthPolicyRequest;
import com.authsphere.server.realm.dto.AuthPolicyResponse;
import com.authsphere.server.realm.mapper.AuthPolicyMapper;
import com.authsphere.server.realm.model.AuthPolicy;
import com.authsphere.server.realm.service.AuthPolicyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证策略管理服务实现，负责认证策略用例流程编排。
 */
@Service
@RequiredArgsConstructor
public class AuthPolicyServiceImpl implements AuthPolicyService {

    private final AuthPolicyMapper authPolicyMapper;
    private final AuthPolicyDomain authPolicyDomain;

    /**
     * 分页查询认证策略，并转换认证方式存储字段。
     *
     * @param request 分页及筛选条件
     * @return 认证策略分页数据
     */
    @Override
    public Page<AuthPolicyResponse> page(AuthPolicyPageRequest request) {
        Page<AuthPolicyResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<AuthPolicyResponse> result = authPolicyMapper.page(page, request);
        authPolicyDomain.enrichListResponses(result.getRecords());
        return result;
    }

    /**
     * 查询启用认证策略选择项。
     *
     * @return 可供身份域或客户端绑定的认证策略选择项
     */
    @Override
    public List<AuthPolicyOptionResponse> listEnabled() {
        return authPolicyMapper.listEnabled();
    }

    /**
     * 查询认证策略详情及引用影响范围。
     *
     * @param id 认证策略主键
     * @return 认证策略详情、引用数量及引用对象明细
     */
    @Override
    public AuthPolicyInfoResponse detail(Long id) {
        return authPolicyDomain.buildInfoResponse(authPolicyDomain.findById(id));
    }

    /**
     * 新增认证策略，系统维护字段由服务端初始化。
     *
     * @param request 认证策略保存参数
     * @return 新增认证策略主键
     */
    @Override
    public Long create(AuthPolicyRequest request) {
        authPolicyDomain.normalizeAndValidateRequest(request);
        authPolicyDomain.checkCodeAvailable(null, request.getCode());
        AuthPolicy policy = AuthPolicyConvert.INSTANCE.model(request);
        policy.setSystemBuiltin(Boolean.FALSE);
        policy.setDeleted(Boolean.FALSE);
        authPolicyMapper.insert(policy);
        return policy.getId();
    }

    /**
     * 编辑认证策略，启用策略切换为禁用前必须先解除引用。
     *
     * @param id 认证策略主键
     * @param request 认证策略保存参数
     * @return 是否编辑成功
     */
    @Override
    public Boolean update(Long id, AuthPolicyRequest request) {
        authPolicyDomain.normalizeAndValidateRequest(request);
        AuthPolicy policy = authPolicyDomain.findById(id);
        if (StatusEnum.NORMAL.getCode().equals(policy.getStatus())
                && StatusEnum.DISABLED.getCode().equals(request.getStatus())) {
            authPolicyDomain.checkNotReferenced(id);
        }
        authPolicyDomain.checkCodeAvailable(id, request.getCode());
        AuthPolicyConvert.INSTANCE.copyToModel(request, policy);
        authPolicyMapper.updateById(policy);
        return Boolean.TRUE;
    }

    /**
     * 复制认证策略。副本不继承系统内置属性和启用状态。
     *
     * @param id 源认证策略主键
     * @param request 副本编码和名称
     * @return 新认证策略主键
     */
    @Override
    public Long copy(Long id, AuthPolicyCopyRequest request) {
        AuthPolicy source = authPolicyDomain.findById(id);
        authPolicyDomain.checkCodeAvailable(null, request.getCode());
        source.setId(null);
        source.setCode(request.getCode());
        source.setName(request.getName());
        source.setStatus(StatusEnum.DISABLED.getCode());
        source.setSystemBuiltin(Boolean.FALSE);
        source.setDeleted(Boolean.FALSE);
        source.setCreateTime(null);
        source.setUpdateTime(null);
        authPolicyMapper.insert(source);
        return source.getId();
    }

    /**
     * 启用认证策略。
     *
     * @param id 认证策略主键
     * @return 是否启用成功
     */
    @Override
    public Boolean enable(Long id) {
        AuthPolicy policy = authPolicyDomain.findById(id);
        policy.setStatus(StatusEnum.NORMAL.getCode());
        authPolicyMapper.updateById(policy);
        return Boolean.TRUE;
    }

    /**
     * 禁用未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否禁用成功
     */
    @Override
    public Boolean disable(Long id) {
        AuthPolicy policy = authPolicyDomain.findById(id);
        authPolicyDomain.checkNotReferenced(id);
        policy.setStatus(StatusEnum.DISABLED.getCode());
        authPolicyMapper.updateById(policy);
        return Boolean.TRUE;
    }

    /**
     * 删除非内置且未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(Long id) {
        AuthPolicy policy = authPolicyDomain.findById(id);
        authPolicyDomain.checkCanDelete(policy);
        authPolicyMapper.deleteById(id);
        return Boolean.TRUE;
    }

    /**
     * 校验认证策略存在且处于启用状态。
     *
     * @param id 认证策略主键
     */
    @Override
    public void validateEnabled(Long id) {
        authPolicyDomain.checkEnabled(id);
    }
}
