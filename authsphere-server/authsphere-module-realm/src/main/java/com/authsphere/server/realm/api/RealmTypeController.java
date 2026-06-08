package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.CreateRealmTypeRequest;
import com.authsphere.server.realm.dto.RealmTypeInfoResponse;
import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.service.RealmTypeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 身份域类型接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class RealmTypeController {

    private final RealmTypeService realmTypeService;

    /**
     * 类型分类分页列表
     */
    @PostMapping("/page")
    public Page<RealmTypePageResponse> page(@Validated @RequestBody RealmTypePageRequest request) {
        return realmTypeService.page(request);
    }

    /**
     * 类型分类列表
     */
    @GetMapping("/list")
    public List<RealmTypePageResponse> list() {
        return realmTypeService.listAll();
    }

    /**
     * 身份域类型详情
     *
     * @param id 身份域类型 ID
     */
    @GetMapping("/{id}")
    public RealmTypeInfoResponse detail(@PathVariable Long id) {
        return realmTypeService.detail(id);
    }

    /**
     * 创建分类项
     *
     */
    @PostMapping
    public Boolean create(@Validated @RequestBody CreateRealmTypeRequest request) {
        return realmTypeService.create(request);
    }

    /**
     * 修改分类项信息
     *
     * @param id 分类项id
     */
    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody CreateRealmTypeRequest request) {
        return realmTypeService.edit(id, request);
    }

    /**
     * 修改分类项状态
     *
     * @param id 分类项id
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable Long id) {
        return realmTypeService.editStatus(id);
    }

    /**
     * 删除分类项
     *
     * @param id 分类项id
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return realmTypeService.delete(id);
    }


}
