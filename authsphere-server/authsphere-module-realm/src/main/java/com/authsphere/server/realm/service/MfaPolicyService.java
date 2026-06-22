package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.MfaPolicyPageRequest;
import com.authsphere.server.realm.dto.MfaPolicyRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * MFA 策略模板服务。
 */
public interface MfaPolicyService {

    Page<MfaPolicyResponse> page(MfaPolicyPageRequest request);

    MfaPolicyResponse getById(Long id);

    MfaPolicyResponse create(MfaPolicyRequest request);

    MfaPolicyResponse update(Long id, MfaPolicyRequest request);

    Boolean editStatus(Long id);

}
