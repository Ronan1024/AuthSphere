package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyPageRequest;
import com.authsphere.server.realm.dto.PasswordPolicyRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 身份域密码策略服务。
 */
public interface PasswordPolicyService {

    /**
     * 分页查询密码策略模板。
     */
    Page<PasswordPolicyListResponse> page(PasswordPolicyPageRequest request);

    /**
     * 查询密码策略模板。
     */
    PasswordPolicyResponse getById(Long id);

    /**
     * 创建密码策略模板。
     */
    PasswordPolicyResponse create(PasswordPolicyRequest request);

    /**
     * 更新密码策略模板。
     */
    PasswordPolicyResponse update(Long id, PasswordPolicyRequest request);

    /**
     * 启用/禁用密码策略模板。
     */
    Boolean editStatus(Long id);

    /**
     * 获取所有的密码策略
     */
    List<PasswordPolicyListResponse> list();
}
