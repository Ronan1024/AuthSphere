package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.AuthMethodDetailResponse;
import com.authsphere.server.realm.dto.AuthMethodOptionResponse;
import com.authsphere.server.realm.dto.AuthMethodPageRequest;
import com.authsphere.server.realm.dto.AuthMethodRequest;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.dto.AuthMethodTemplateResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 认证方式管理服务。
 */
public interface AuthMethodService {

    Page<AuthMethodResponse> page(AuthMethodPageRequest request);

    List<AuthMethodOptionResponse> listEnabled(String position);

    List<AuthMethodTemplateResponse> listTemplates();

    AuthMethodDetailResponse detail(Long id);

    Long create(AuthMethodRequest request);

    Boolean update(Long id, AuthMethodRequest request);

    Boolean enable(Long id);

    Boolean disable(Long id);

    Boolean delete(Long id);

    void validateEnabledCodes(List<String> codes);
}
