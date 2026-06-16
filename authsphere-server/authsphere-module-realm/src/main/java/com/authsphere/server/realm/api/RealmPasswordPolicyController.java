package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.authsphere.server.realm.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身份域密码策略绑定管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/realm/{realmId}/password-policy")
public class RealmPasswordPolicyController {

    private final PasswordPolicyService passwordPolicyService;

    /**
     * 查询身份域已绑定的密码策略模板。
     */
    @GetMapping
    public PasswordPolicyResponse get(@PathVariable("realmId") Long realmId) {
        return passwordPolicyService.getRealmPolicy(realmId);
    }

    /**
     * 绑定密码策略模板。
     */
    @PutMapping("/{policyId}")
    public Boolean bind(@PathVariable("realmId") Long realmId, @PathVariable("policyId") Long policyId) {
        return passwordPolicyService.bindRealmPolicy(realmId, policyId);
    }
}
