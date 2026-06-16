package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.UniquePolicyPageRequest;
import com.authsphere.server.realm.dto.UniquePolicyRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 账号唯一性规则模板服务。
 */
public interface UniquePolicyService {

    Page<UniquePolicyResponse> page(UniquePolicyPageRequest request);

    UniquePolicyResponse getById(Long id);

    UniquePolicyResponse create(UniquePolicyRequest request);

    UniquePolicyResponse update(Long id, UniquePolicyRequest request);

    Boolean editStatus(Long id);

    UniquePolicyResponse getRealmPolicy(Long realmId);

    Boolean bindRealmPolicy(Long realmId, Long policyId);
}
