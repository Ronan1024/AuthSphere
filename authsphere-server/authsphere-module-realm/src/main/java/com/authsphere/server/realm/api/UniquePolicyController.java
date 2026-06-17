package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.UniquePolicyPageRequest;
import com.authsphere.server.realm.dto.UniquePolicyRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.service.UniquePolicyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号唯一性规则模板管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/unique-policy")
public class UniquePolicyController {

    private final UniquePolicyService uniquePolicyService;

    /**
     * 账号唯一性规则模板分页列表。
     */
    @PostMapping("/page")
    public Page<UniquePolicyResponse> page(@RequestBody @Validated UniquePolicyPageRequest request) {
        return uniquePolicyService.page(request);
    }

    /**
     * 账号唯一性规则模板详情。
     */
    @GetMapping("/{id}")
    public UniquePolicyResponse get(@PathVariable("id") Long id) {
        return uniquePolicyService.getById(id);
    }

    /**
     * 创建账号唯一性规则模板。
     */
    @PostMapping
    public UniquePolicyResponse create(@RequestBody @Validated UniquePolicyRequest request) {
        return uniquePolicyService.create(request);
    }

    /**
     * 更新账号唯一性规则模板。
     */
    @PutMapping("/{id}")
    public UniquePolicyResponse update(@PathVariable("id") Long id, @RequestBody @Validated UniquePolicyRequest request) {
        return uniquePolicyService.update(id, request);
    }

    /**
     * 启用/禁用账号唯一性规则模板。
     */
    @PutMapping("/status/{id}")
    public Boolean status(@PathVariable("id") Long id) {
        return uniquePolicyService.editStatus(id);
    }
}
