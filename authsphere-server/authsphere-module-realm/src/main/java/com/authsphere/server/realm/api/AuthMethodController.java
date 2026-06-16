package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.AuthMethodDetailResponse;
import com.authsphere.server.realm.dto.AuthMethodOptionResponse;
import com.authsphere.server.realm.dto.AuthMethodPageRequest;
import com.authsphere.server.realm.dto.AuthMethodRequest;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.dto.AuthMethodTemplateResponse;
import com.authsphere.server.realm.service.AuthMethodService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证方式管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/admin/auth/method", "/api/iam/auth-methods"})
public class AuthMethodController {

    private final AuthMethodService authMethodService;

    /**
     * 分页查询认证方式。
     *
     * @param request 分页及筛选条件
     * @return 认证方式分页数据
     */
    @PostMapping("/page")
    public Page<AuthMethodResponse> page(@Validated @RequestBody AuthMethodPageRequest request) {
        return authMethodService.page(request);
    }

    /**
     * 查询启用认证方式选择项。
     *
     * @param position 可用位置；为空表示全部
     * @return 启用认证方式选择项
     */
    @GetMapping({"", "/list"})
    public List<AuthMethodOptionResponse> listEnabled(@RequestParam(required = false) String position) {
        return authMethodService.listEnabled(position);
    }

    /**
     * 查询系统内置认证方式模板默认名称和编码。
     *
     * @return 内置认证方式模板
     */
    @GetMapping("/templates")
    public List<AuthMethodTemplateResponse> templates() {
        return authMethodService.listTemplates();
    }

    /**
     * 查询认证方式详情、配置参数和引用策略。
     *
     * @param id 认证方式主键
     * @return 认证方式详情
     */
    @GetMapping("/{id}")
    public AuthMethodDetailResponse detail(@PathVariable Long id) {
        return authMethodService.detail(id);
    }

    /**
     * 新增认证方式。
     *
     * @param request 保存请求
     * @return 新增认证方式主键
     */
    @PostMapping
    public Long create(@Validated @RequestBody AuthMethodRequest request) {
        return authMethodService.create(request);
    }

    /**
     * 编辑认证方式。
     *
     * @param id 认证方式主键
     * @param request 保存请求
     * @return 是否编辑成功
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @Validated @RequestBody AuthMethodRequest request) {
        return authMethodService.update(id, request);
    }

    /**
     * 启用认证方式。
     *
     * @param id 认证方式主键
     * @return 是否启用成功
     */
    @PutMapping("/{id}/enable")
    public Boolean enable(@PathVariable Long id) {
        return authMethodService.enable(id);
    }

    /**
     * 禁用认证方式。已有引用会保留并在详情中展示风险范围。
     *
     * @param id 认证方式主键
     * @return 是否禁用成功
     */
    @PutMapping("/{id}/disable")
    public Boolean disable(@PathVariable Long id) {
        return authMethodService.disable(id);
    }

    /**
     * 删除非内置且未被认证策略引用的认证方式。
     *
     * @param id 认证方式主键
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return authMethodService.delete(id);
    }
}
