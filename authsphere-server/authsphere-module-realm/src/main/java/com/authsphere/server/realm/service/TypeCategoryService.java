package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.CreateTypeCategoryRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageResponse;
import com.authsphere.server.realm.model.TypeCategory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【type_category(类型项)】的数据库操作Service
 * @createDate 2026-05-28 06:04:57
 */
public interface TypeCategoryService extends IService<TypeCategory> {

    /**
     * 类型分类分页列表。
     */
    Page<TypeCategoryPageResponse> page(TypeCategoryPageRequest request);

    /**
     * 类型分类列表。
     */
    List<TypeCategoryPageResponse> listAll();

    /**
     * 提交分类项信息
     */
    Boolean create(CreateTypeCategoryRequest request);

    /**
     * 修改分类项信息
     * @param id 分类项Id
     */
    Boolean edit(Long id, CreateTypeCategoryRequest request);

    /**
     * 修改分类项状态
     * @param id 分类项Id
     */
    Boolean editStatus(Long id);
}
