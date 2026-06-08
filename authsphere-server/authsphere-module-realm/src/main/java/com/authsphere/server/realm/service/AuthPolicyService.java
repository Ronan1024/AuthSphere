package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.AuthPolicyCopyRequest;
import com.authsphere.server.realm.dto.AuthPolicyInfoResponse;
import com.authsphere.server.realm.dto.AuthPolicyOptionResponse;
import com.authsphere.server.realm.dto.AuthPolicyPageRequest;
import com.authsphere.server.realm.dto.AuthPolicyRequest;
import com.authsphere.server.realm.dto.AuthPolicyResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 认证策略管理服务。
 */
public interface AuthPolicyService {

    /**
     * 分页查询认证策略。
     *
     * @param request 分页及筛选条件
     * @return 认证策略分页数据
     */
    Page<AuthPolicyResponse> page(AuthPolicyPageRequest request);

    /**
     * 查询启用认证策略选择项。
     *
     * @return 可供身份域或客户端绑定的认证策略选择项
     */
    List<AuthPolicyOptionResponse> listEnabled();

    /**
     * 查询认证策略详情及引用影响范围。
     *
     * @param id 认证策略主键
     * @return 认证策略详情、引用数量及引用对象明细
     */
    AuthPolicyInfoResponse detail(Long id);

    /**
     * 新增认证策略。
     *
     * @param request 认证策略保存参数
     * @return 新增认证策略主键
     */
    Long create(AuthPolicyRequest request);

    /**
     * 编辑认证策略。
     *
     * @param id 认证策略主键
     * @param request 认证策略保存参数
     * @return 是否编辑成功
     */
    Boolean update(Long id, AuthPolicyRequest request);

    /**
     * 复制认证策略，副本默认禁用。
     *
     * @param id 源认证策略主键
     * @param request 副本编码和名称
     * @return 新认证策略主键
     */
    Long copy(Long id, AuthPolicyCopyRequest request);

    /**
     * 启用认证策略。
     *
     * @param id 认证策略主键
     * @return 是否启用成功
     */
    Boolean enable(Long id);

    /**
     * 禁用未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否禁用成功
     */
    Boolean disable(Long id);

    /**
     * 删除非内置且未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 校验认证策略存在且处于启用状态。
     *
     * @param id 认证策略主键
     */
    void validateEnabled(Long id);
}
