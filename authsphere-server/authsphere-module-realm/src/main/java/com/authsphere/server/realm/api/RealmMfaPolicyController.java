package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.service.MfaPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身份域 MFA 策略绑定管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/realm/{realmId}/mfa-policy")
public class RealmMfaPolicyController {

    private final MfaPolicyService mfaPolicyService;

    /**
     * 查询身份域已绑定的 MFA 策略模板。
     */
    @GetMapping
    public MfaPolicyResponse get(@PathVariable("realmId") Long realmId) {
        return mfaPolicyService.getRealmPolicy(realmId);
    }

    /**
     * 绑定 MFA 策略模板。
     */
    @PutMapping("/{policyId}")
    public Boolean bind(@PathVariable("realmId") Long realmId, @PathVariable("policyId") Long policyId) {
        return mfaPolicyService.bindRealmPolicy(realmId, policyId);
    }
}
