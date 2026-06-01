package com.authsphere.server.realm.domain;

import com.authsphere.server.common.model.R;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.Realm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/1
 */
@Service
@RequiredArgsConstructor
public class RealmDomain {

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
                .eq(Realm::getTypeCategoryId, typeId)
        );
    }
}
