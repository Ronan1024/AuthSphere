package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.service.UniquePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身份域账号唯一性规则绑定管理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/realm/{realmId}/unique-policy")
public class RealmUniquePolicyController {

    private final UniquePolicyService uniquePolicyService;

    /**
     * 查询身份域已绑定的账号唯一性规则模板。
     */
    @GetMapping
    public UniquePolicyResponse get(@PathVariable("realmId") Long realmId) {
        return uniquePolicyService.getRealmPolicy(realmId);
    }

    /**
     * 绑定账号唯一性规则模板。
     */
    @PutMapping("/{policyId}")
    public Boolean bind(@PathVariable("realmId") Long realmId, @PathVariable("policyId") Long policyId) {
        return uniquePolicyService.bindRealmPolicy(realmId, policyId);
    }
}
