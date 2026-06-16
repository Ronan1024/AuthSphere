package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.realm.convert.LoginPageConvert;
import com.authsphere.server.realm.domain.LoginPageDomain;
import com.authsphere.server.realm.dto.LoginPageInfoResponse;
import com.authsphere.server.realm.dto.LoginPageOptionResponse;
import com.authsphere.server.realm.dto.LoginPagePageRequest;
import com.authsphere.server.realm.dto.LoginPagePreviewResponse;
import com.authsphere.server.realm.dto.LoginPageRequest;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.authsphere.server.realm.mapper.LoginPageMapper;
import com.authsphere.server.realm.model.LoginPage;
import com.authsphere.server.realm.service.LoginPageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 登录页管理服务实现，负责登录页用例流程编排。
 */
@Service
@RequiredArgsConstructor
public class LoginPageServiceImpl implements LoginPageService {

    private final LoginPageMapper loginPageMapper;
    private final LoginPageDomain loginPageDomain;

    /**
     * 分页查询登录页列表。
     *
     * @param request 分页查询条件
     * @return 登录页分页数据
     */
    @Override
    public Page<LoginPageResponse> page(LoginPagePageRequest request) {
        Page<LoginPageResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<LoginPageResponse> result = loginPageMapper.page(page, request);

        List<LoginPageResponse> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        records.forEach(e -> e.setReferenceCount(loginPageMapper.countClientReferences(e.getId())));
        return result;
    }

    /**
     * 查询启用状态的登录页选项。
     *
     * @return 登录页选项
     */
    @Override
    public List<LoginPageOptionResponse> listEnabled() {
        return loginPageMapper.listEnabled();
    }

    /**
     * 查询登录页详情。
     *
     * @param id 登录页ID
     * @return 登录页详情
     */
    @Override
    public LoginPageInfoResponse detail(Long id) {
        LoginPage loginPage = loginPageDomain.findById(id);


        return LoginPageConvert.INSTANCE.toLoginPageInfoResponse(loginPage);

    }

    /**
     * 查询登录页预览配置。
     *
     * @param id 登录页ID
     * @return 登录页预览配置
     */
    @Override
    public LoginPagePreviewResponse preview(Long id) {
        return loginPageDomain.buildPreviewResponse(loginPageDomain.findById(id));
    }

    /**
     * 新增登录页。
     *
     * @param request 登录页保存参数
     * @return 新增登录页ID
     */
    @Override
    public Long create(LoginPageRequest request) {
        loginPageDomain.normalizeAndValidateRequest(request);
        loginPageDomain.checkApplicableRealmTypeExists(request.getApplicableRealmTypeId());
        loginPageDomain.checkCodeAvailable(null, request.getCode());
        LoginPage loginPage = LoginPageConvert.INSTANCE.model(request);
        loginPage.setSystemBuiltin(Boolean.FALSE);
        loginPage.setDeleted(Boolean.FALSE);
        loginPageMapper.insert(loginPage);
        return loginPage.getId();
    }

    /**
     * 编辑登录页。启用状态切换为禁用状态前，必须先解除所有引用。
     *
     * @param id      登录页ID
     * @param request 登录页保存参数
     * @return 是否更新成功
     */
    @Override
    public Boolean update(Long id, LoginPageRequest request) {
        loginPageDomain.normalizeAndValidateRequest(request);
        loginPageDomain.checkApplicableRealmTypeExists(request.getApplicableRealmTypeId());
        LoginPage loginPage = loginPageDomain.findById(id);
        if (StatusEnum.NORMAL.getCode().equals(loginPage.getStatus())
                && StatusEnum.DISABLED.getCode().equals(request.getStatus())) {
            loginPageDomain.checkNotReferenced(id);
        }
        loginPageDomain.checkCodeAvailable(id, request.getCode());
        LoginPageConvert.INSTANCE.copyToModel(request, loginPage);
        loginPageMapper.updateById(loginPage);
        return Boolean.TRUE;
    }

    /**
     * 启用登录页。
     *
     * @param id 登录页ID
     * @return 是否启用成功
     */
    @Override
    public Boolean enable(Long id) {
        LoginPage loginPage = loginPageDomain.findById(id);
        loginPage.setStatus(StatusEnum.NORMAL.getCode());
        loginPageMapper.updateById(loginPage);
        return Boolean.TRUE;
    }

    /**
     * 禁用登录页。被引用的登录页不能禁用。
     *
     * @param id 登录页ID
     * @return 是否禁用成功
     */
    @Override
    public Boolean disable(Long id) {
        LoginPage loginPage = loginPageDomain.findById(id);
        loginPageDomain.checkNotReferenced(id);
        loginPage.setStatus(StatusEnum.DISABLED.getCode());
        loginPageMapper.updateById(loginPage);
        return Boolean.TRUE;
    }

    /**
     * 删除登录页。系统内置或被引用的登录页不能删除。
     *
     * @param id 登录页ID
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(Long id) {
        LoginPage loginPage = loginPageDomain.findById(id);
        loginPageDomain.checkCanDelete(loginPage);
        loginPageMapper.deleteById(id);
        return Boolean.TRUE;
    }

    /**
     * 校验登录页存在且处于启用状态。
     *
     * @param id 登录页ID
     */
    @Override
    public void validateEnabled(Long id) {
        loginPageDomain.checkEnabled(id);
    }
}
