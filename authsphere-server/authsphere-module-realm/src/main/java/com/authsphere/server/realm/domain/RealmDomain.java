package com.authsphere.server.realm.domain;

import com.authsphere.server.api.RealmApi;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/1
 */
@Service
@RequiredArgsConstructor
public class RealmDomain implements RealmApi {

    private final RealmMapper realmMapper;

    /**
     * 获取身份域信息
     */
    public Realm findById(Long id) {
        return realmMapper.selectById(id);
    }


    /**
     * 根据类型ID获取身份域列表
     */
    public List<Realm> findListByType(Long typeId) {
        return realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .eq(Realm::getRealmTypeId, typeId)
        );
    }

    /**
     * 获取身份域信息
     *
     * @param realmId
     */
    @Override
    public RealmInfoResponse info(Long realmId) {
        Realm byId = findById(realmId);
        if (ObjectUtils.isEmpty(byId)) {
            throw new BizException(RealmErrorCode.REALM_DATA_ERROR);
        }
        return RealmConvert.INSTANCE.toRealmInfoResponse(byId);
    }

    /**
     * 获取身份域信息
     */
    @Override
    public List<RealmInfoResponse> list(List<Long> realmIdList) {
        List<Realm> realms = realmMapper.selectBatchIds(realmIdList);
        return RealmConvert.INSTANCE.toRealmInfoResponse(realms);
    }

    /**
     * 根据类型ID获取身份域列表
     * @param typeIdList 身份域类型
     */
    public List<Realm> findListByType(List<Long> typeIdList) {
        return realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .in(Realm::getRealmTypeId, typeIdList)
        );
    }
}
