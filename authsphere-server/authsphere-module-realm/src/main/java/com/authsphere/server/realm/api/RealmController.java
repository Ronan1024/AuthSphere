package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmDetailResponse;
import com.authsphere.server.realm.dto.RealmListResponse;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.service.RealmService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 身份域管理
 *
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/realm")
public class RealmController {

    private final RealmService realmService;


    /**
     * 身份域分页列表。
     *
     * <p>返回列表页展示所需的摘要信息，
     * 包括基础信息、默认认证方式、支持的认证方式摘要和引用统计。</p>
     */
    @PostMapping("/page")
    public Page<RealmPageResponse> page(@RequestBody @Validated RealmPageRequest realmPageRequest) {
        return realmService.page(realmPageRequest);
    }


    /**
     * 创建身份域。
     *
     * <p>一次性保存基础信息、SSO、认证方式绑定和直接安全配置。</p>
     */
    @PostMapping
    public Boolean create(@RequestBody @Validated CreateRealmRequest createRealmRequest) {
        return realmService.create(createRealmRequest);
    }

    /**
     * 修改身份域。
     *
     * @param id 身份域 ID
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @RequestBody @Validated CreateRealmRequest createRealmRequest) {
        return realmService.update(id, createRealmRequest);
    }

    /**
     * 查询身份域详情。
     *
     * <p>用于详情页展示和编辑页回填，返回完整认证与安全配置。</p>
     */
    @GetMapping("/{id}")
    public RealmDetailResponse detail(@PathVariable Long id) {
        return realmService.detail(id);
    }


    /**
     * 启用/禁用身份域。
     */
    @PutMapping("/status/{id}")
    public Boolean status(@PathVariable Long id) {
        return realmService.editStatus(id);
    }

    /**
     * 删除身份域。
     *
     * <p>删除前会校验账号和客户端引用，避免把仍在使用的身份域直接删掉。</p>
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return realmService.delete(id);
    }

    /**
     * 获取启用状态的身份域列表。
     *
     * <p>用于下拉选择等轻量场景，只返回最小字段集。</p>
     */
    @GetMapping("/list")
    public List<RealmListResponse> list() {
        return realmService.list();
    }

}
