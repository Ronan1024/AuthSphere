package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.MfaPolicyPageRequest;
import com.authsphere.server.realm.dto.MfaPolicyRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.service.MfaPolicyService;
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
 * MFA 策略模板管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mfa-policy")
public class MfaPolicyController {

    private final MfaPolicyService mfaPolicyService;

    /**
     * MFA 策略模板分页列表。
     */
    @PostMapping("/page")
    public Page<MfaPolicyResponse> page(@RequestBody @Validated MfaPolicyPageRequest request) {
        return mfaPolicyService.page(request);
    }

    /**
     * MFA 策略模板详情。
     */
    @GetMapping("/{id}")
    public MfaPolicyResponse get(@PathVariable("id") Long id) {
        return mfaPolicyService.getById(id);
    }

    /**
     * 创建 MFA 策略模板。
     */
    @PostMapping
    public MfaPolicyResponse create(@RequestBody @Validated MfaPolicyRequest request) {
        return mfaPolicyService.create(request);
    }

    /**
     * 更新 MFA 策略模板。
     */
    @PutMapping("/{id}")
    public MfaPolicyResponse update(@PathVariable("id") Long id, @RequestBody @Validated MfaPolicyRequest request) {
        return mfaPolicyService.update(id, request);
    }

    /**
     * 启用/禁用 MFA 策略模板。
     */
    @PutMapping("/status/{id}")
    public Boolean status(@PathVariable("id") Long id) {
        return mfaPolicyService.editStatus(id);
    }
}
