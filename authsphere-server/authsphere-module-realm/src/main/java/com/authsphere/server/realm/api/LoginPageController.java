package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.LoginPageInfoResponse;
import com.authsphere.server.realm.dto.LoginPagePageRequest;
import com.authsphere.server.realm.dto.LoginPageOptionResponse;
import com.authsphere.server.realm.dto.LoginPagePreviewResponse;
import com.authsphere.server.realm.dto.LoginPageRequest;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.authsphere.server.realm.service.LoginPageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录页管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/admin/login/page", "/api/iam/login-pages"})
public class LoginPageController {

    private final LoginPageService loginPageService;

    /**
     * 登录页分页列表。
     *
     * @param request 分页查询条件
     * @return 登录页分页数据
     */
    @PostMapping("/page")
    public Page<LoginPageResponse> page(@Validated @RequestBody LoginPagePageRequest request) {
        return loginPageService.page(request);
    }

    /**
     * 可选登录页列表，仅返回启用状态数据。
     *
     * @return 启用登录页选项
     */
    @GetMapping({"", "/list"})
    public List<LoginPageOptionResponse> list() {
        return loginPageService.listEnabled();
    }

    /**
     * 登录页详情。
     *
     * @param id 登录页ID
     * @return 登录页详情
     */
    @GetMapping("/{id}")
    public LoginPageInfoResponse detail(@PathVariable Long id) {
        return loginPageService.detail(id);
    }

    /**
     * 登录页预览配置。
     *
     * @param id 登录页ID
     * @return 登录页预览配置
     */
    @GetMapping("/{id}/preview")
    public LoginPagePreviewResponse preview(@PathVariable Long id) {
        return loginPageService.preview(id);
    }

    /**
     * 新增登录页。
     *
     * @param request 登录页保存参数
     * @return 新增登录页ID
     */
    @PostMapping
    public Long create(@Validated @RequestBody LoginPageRequest request) {
        return loginPageService.create(request);
    }

    /**
     * 编辑登录页。
     *
     * @param id 登录页ID
     * @param request 登录页保存参数
     * @return 是否更新成功
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @Validated @RequestBody LoginPageRequest request) {
        return loginPageService.update(id, request);
    }

    /**
     * 启用登录页。
     *
     * @param id 登录页ID
     * @return 是否启用成功
     */
    @PutMapping("/{id}/enable")
    public Boolean enable(@PathVariable Long id) {
        return loginPageService.enable(id);
    }

    /**
     * 禁用登录页。
     *
     * @param id 登录页ID
     * @return 是否禁用成功
     */
    @PutMapping("/{id}/disable")
    public Boolean disable(@PathVariable Long id) {
        return loginPageService.disable(id);
    }

    /**
     * 删除登录页。
     *
     * @param id 登录页ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return loginPageService.delete(id);
    }
}
