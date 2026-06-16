package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyPageRequest;
import com.authsphere.server.realm.dto.PasswordPolicyRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.realm.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 密码策略模板管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/password/policy")
public class PasswordPolicyController {

    private final PasswordPolicyService passwordPolicyService;

    /**
     * 密码策略模板分页列表。
     */
    @PostMapping("/page")
    public Page<PasswordPolicyListResponse> page(@RequestBody @Validated PasswordPolicyPageRequest request) {
        return passwordPolicyService.page(request);
    }

    /**
     * 密码策略模板详情。
     */
    @GetMapping("/{id}")
    public PasswordPolicyResponse info(@PathVariable Long id) {
        return passwordPolicyService.getById(id);
    }

    /**
     * 创建密码策略模板。
     */
    @PostMapping
    public PasswordPolicyResponse create(@RequestBody @Validated PasswordPolicyRequest request) {
        return passwordPolicyService.create(request);
    }

    /**
     * 更新密码策略模板。
     */
    @PutMapping("/{id}")
    public PasswordPolicyResponse update(@PathVariable Long id,
                                         @RequestBody @Validated PasswordPolicyRequest request) {
        return passwordPolicyService.update(id, request);
    }

    /**
     * 启用/禁用密码策略模板。
     */
    @PutMapping("/status/{id}")
    public Boolean status(@PathVariable Long id) {
        return passwordPolicyService.editStatus(id);
    }


    /**
     * 获取所有的密码策略
     */
    @GetMapping("/list")
    public List<PasswordPolicyListResponse> list(){
       return passwordPolicyService.list();
    }
}
