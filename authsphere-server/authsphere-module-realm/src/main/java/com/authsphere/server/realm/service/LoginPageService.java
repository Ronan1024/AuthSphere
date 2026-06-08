package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.LoginPageInfoResponse;
import com.authsphere.server.realm.dto.LoginPagePageRequest;
import com.authsphere.server.realm.dto.LoginPageOptionResponse;
import com.authsphere.server.realm.dto.LoginPagePreviewResponse;
import com.authsphere.server.realm.dto.LoginPageRequest;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 登录页管理服务。
 */
public interface LoginPageService {

    /**
     * 分页查询登录页列表。
     *
     * @param request 分页查询条件
     * @return 登录页分页数据
     */
    Page<LoginPageResponse> page(LoginPagePageRequest request);

    /**
     * 查询可供身份域或客户端选择的启用登录页。
     *
     * @return 启用登录页选项
     */
    List<LoginPageOptionResponse> listEnabled();

    /**
     * 查询登录页详情。
     *
     * @param id 登录页ID
     * @return 登录页详情
     */
    LoginPageInfoResponse detail(Long id);

    /**
     * 查询登录页预览配置。
     *
     * @param id 登录页ID
     * @return 登录页预览配置
     */
    LoginPagePreviewResponse preview(Long id);

    /**
     * 新增登录页。
     *
     * @param request 登录页保存参数
     * @return 新增登录页ID
     */
    Long create(LoginPageRequest request);

    /**
     * 编辑登录页。
     *
     * @param id 登录页ID
     * @param request 登录页保存参数
     * @return 是否更新成功
     */
    Boolean update(Long id, LoginPageRequest request);

    /**
     * 启用登录页。
     *
     * @param id 登录页ID
     * @return 是否启用成功
     */
    Boolean enable(Long id);

    /**
     * 禁用登录页。
     *
     * @param id 登录页ID
     * @return 是否禁用成功
     */
    Boolean disable(Long id);

    /**
     * 删除登录页。
     *
     * @param id 登录页ID
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 校验登录页存在且处于启用状态。
     *
     * @param id 登录页ID
     */
    void validateEnabled(Long id);
}
