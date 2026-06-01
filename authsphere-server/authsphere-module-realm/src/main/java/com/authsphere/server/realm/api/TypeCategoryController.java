package com.authsphere.server.realm.api;

import com.authsphere.server.realm.dto.CreateTypeCategoryRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageResponse;
import com.authsphere.server.realm.service.TypeCategoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class TypeCategoryController {

    private final TypeCategoryService typeCategoryService;

    /**
     * 类型分类分页列表。
     */
    @PostMapping("/page")
    public Page<TypeCategoryPageResponse> page(@Validated @RequestBody TypeCategoryPageRequest request) {
        return typeCategoryService.page(request);
    }

    /**
     * 类型分类列表。
     */
    @GetMapping("/list")
    public List<TypeCategoryPageResponse> list() {
        return typeCategoryService.listAll();
    }

    @PostMapping
    public Boolean create(@Validated @RequestBody CreateTypeCategoryRequest request) {
        return typeCategoryService.create(request);
    }

    /**
     * 修改分类项信息
     *
     * @param id 分类项id
     */
    @PutMapping("/{id}")
    public Boolean edit(@PathVariable Long id, @Validated @RequestBody CreateTypeCategoryRequest request) {
        return typeCategoryService.edit(id, request);
    }


    /**
     * 修改分类项状态
     *
     * @param id 分类项id
     */
    @PutMapping("/status/{id}")
    public Boolean editStatus(@PathVariable Long id) {
        return typeCategoryService.editStatus(id);
    }



}
