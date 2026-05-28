package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.service.RealmService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 身份域分页列表
     */
    @PostMapping("/page")
    public Page<RealmPageResponse> page(@RequestBody @Validated RealmPageRequest realmPageRequest) {
        return realmService.page(realmPageRequest);
    }


    /**
     * 创建身份域信息
     */
    @PostMapping
    public Boolean create(@RequestBody @Validated CreateRealmRequest createRealmRequest) {
        return realmService.create(createRealmRequest);
    }

    /**
     * 修改身份域信息
     *
     * @param id 身份域id
     */
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @RequestBody @Validated CreateRealmRequest createRealmRequest) {
        return realmService.update(id, createRealmRequest);
    }


    /**
     * 启用/禁用
     */
    @PutMapping("/status/{id}")
    public Boolean status(@PathVariable Long id) {
        return realmService.editStatus(id);
    }

}
