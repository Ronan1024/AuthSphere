package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.CreateRealmTypeRequest;
import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.dto.RealmTypeInfoResponse;
import com.authsphere.server.realm.model.RealmType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author longjiangran
 * @description 针对表【type_category(类型项)】的数据库操作Service
 * @createDate 2026-05-28 06:04:57
 */
public interface RealmTypeService extends IService<RealmType> {

    /**
     * 类型分类分页列表。
     */
    Page<RealmTypePageResponse> page(RealmTypePageRequest request);

    /**
     * 类型分类列表。
     */
    List<RealmTypePageResponse> listAll();

    /**
     * 身份域类型详情。
     *
     * @param id 身份域类型 ID
     */
    RealmTypeInfoResponse detail(Long id);

    /**
     * 提交分类项信息
     */
    Boolean create(CreateRealmTypeRequest request);

    /**
     * 修改分类项信息
     * @param id 分类项Id
     */
    Boolean edit(Long id, CreateRealmTypeRequest request);

    /**
     * 修改分类项状态
     * @param id 分类项Id
     */
    Boolean editStatus(Long id);

    /**
     * 删除分类项
     * @param id 分类项Id
     */
    Boolean delete(Long id);
}

