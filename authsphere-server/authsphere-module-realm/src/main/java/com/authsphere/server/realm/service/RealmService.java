package com.authsphere.server.realm.service;

import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author longjiangran
 * @description 针对表【realm(身份域信息)】的数据库操作Service
 * @createDate 2026-05-26 14:40:36
 */
public interface RealmService {

    /**
     * 创建身份域信息
     */
    Boolean create(CreateRealmRequest createRealmRequest);

    /**
     * 更新身份域信息
     *
     * @param id 身份域id
     */
    Boolean update(Long id, CreateRealmRequest createRealmRequest);


    /**
     * 修改身份域状态 修改当前状态的取反状态
     *
     * @param id 状态域id
     */
    Boolean editStatus(Long id);

    /**
     * 身份域分页列表
     */
    Page<RealmPageResponse> page(RealmPageRequest realmPageRequest);

}
