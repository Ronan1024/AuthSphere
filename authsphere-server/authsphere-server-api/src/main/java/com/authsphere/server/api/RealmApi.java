package com.authsphere.server.api;

import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/2
 */
public interface RealmApi {

    /**
     * 获取身份域信息
     */
    RealmInfoResponse info(Long realmId);


    /**
     * 获取身份域信息列表
     */
    List<RealmInfoResponse> list(List<Long> realmIdList);

}
