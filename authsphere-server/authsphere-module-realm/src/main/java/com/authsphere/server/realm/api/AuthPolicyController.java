package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.AuthPolicyCopyRequest;
import com.authsphere.server.realm.dto.AuthPolicyInfoResponse;
import com.authsphere.server.realm.dto.AuthPolicyOptionResponse;
import com.authsphere.server.realm.dto.AuthPolicyPageRequest;
import com.authsphere.server.realm.dto.AuthPolicyRequest;
import com.authsphere.server.realm.dto.AuthPolicyResponse;
import com.authsphere.server.realm.service.AuthPolicyService;
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
 * 认证策略管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/admin/auth/policy", "/api/iam/auth-policies"})
public class AuthPolicyController {

    private final AuthPolicyService authPolicyService;

    /**
     * 分页查询认证策略。
     *
     * @param request 分页及筛选条件
     * @return 认证策略分页数据
     */
    @PostMapping("/page")
    public Page<AuthPolicyResponse> page(@Validated @RequestBody AuthPolicyPageRequest request) {
        return authPolicyService.page(request);
    }

    /**
     * 查询启用认证策略选择项。
     *
     * @return 可供身份域或客户端绑定的认证策略选择项
     */
    @GetMapping({"", "/list"})
    public List<AuthPolicyOptionResponse> listEnabled() {
        return authPolicyService.listEnabled();
    }

    /**
     * 查询认证策略详情及引用影响范围。
     *
     * @param id 认证策略主键
     * @return 认证策略详情、引用数量及引用对象明细
     */
    @GetMapping("/{id}")
    public AuthPolicyInfoResponse detail(@PathVariable Long id) {
        return authPolicyService.detail(id);
    }

    /**
     * 新增认证策略。
     *
     * @param request 认证策略保存参数
     * @return 新增认证策略主键
     */
    @PostMapping
    public Long create(@Validated @RequestBody AuthPolicyRequest request) {
        return authPolicyService.create(request);
    }

    /**
     * 编辑认证策略。
     *
     * @param id 认证策略主键
     * @param request 认证策略保存参数
     * @return 是否编辑成功
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @Validated @RequestBody AuthPolicyRequest request) {
        return authPolicyService.update(id, request);
    }

    /**
     * 复制认证策略，副本默认禁用。
     *
     * @param id 源认证策略主键
     * @param request 副本编码和名称
     * @return 新认证策略主键
     */
    @PostMapping("/{id}/copy")
    public Long copy(@PathVariable Long id, @Validated @RequestBody AuthPolicyCopyRequest request) {
        return authPolicyService.copy(id, request);
    }

    /**
     * 启用认证策略。
     *
     * @param id 认证策略主键
     * @return 是否启用成功
     */
    @PutMapping("/{id}/enable")
    public Boolean enable(@PathVariable Long id) {
        return authPolicyService.enable(id);
    }

    /**
     * 禁用未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否禁用成功
     */
    @PutMapping("/{id}/disable")
    public Boolean disable(@PathVariable Long id) {
        return authPolicyService.disable(id);
    }

    /**
     * 删除非内置且未被引用的认证策略。
     *
     * @param id 认证策略主键
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return authPolicyService.delete(id);
    }
}
