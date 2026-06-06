package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.domain.TypeCategoryDomain;
import com.authsphere.server.realm.dto.AuthMethodInfoResponse;
import com.authsphere.server.realm.dto.CreateRealmRequest;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.TypeCategory;
import com.authsphere.server.realm.service.RealmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.authsphere.server.common.enums.StatusEnum.DISABLED;
import static com.authsphere.server.common.enums.StatusEnum.NORMAL;
import static com.authsphere.server.realm.error.RealmErrorCode.REALM_DATA_ERROR;

/**
 * @author longjiangran
 * @description 针对表【realm(身份域信息)】的数据库操作Service实现
 * @createDate 2026-05-26 14:40:36
 */
@Service
@RequiredArgsConstructor
public class RealmServiceImpl implements RealmService {
    private final RealmMapper realmMapper;
    private final TypeCategoryDomain typeCategoryDomain;

    /**
     * 创建身份域信息
     *
     */
    @Override
    public Boolean create(CreateRealmRequest createRealmRequest) {
        List<Realm> realms = realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .eq(Realm::getCode, createRealmRequest.getCode()));
        if (!CollectionUtils.isEmpty(realms)) {
            throw new BizException(RealmErrorCode.REALM_CODE_EXISTS);
        }

        Realm realm = RealmConvert.INSTANCE.model(createRealmRequest);
        realm.setStatus(NORMAL.getCode());
        realmMapper.insert(realm);
        return Boolean.TRUE;
    }

    /**
     * 更新身份域信息
     *
     * @param id 身份域id
     */
    @Override
    public Boolean update(Long id, CreateRealmRequest createRealmRequest) {
        Realm realm = findById(id);
        RealmConvert.INSTANCE.copyByModel(createRealmRequest, realm);
        realmMapper.updateById(realm);
        return Boolean.TRUE;
    }

    /**
     * 修改身份域状态 修改当前状态的取反状态
     *
     * @param id 状态域id
     */
    @Override
    public Boolean editStatus(Long id) {
        Realm realm = findById(id);
        Integer status = realm.getStatus().equals(NORMAL.getCode()) ? DISABLED.getCode() : NORMAL.getCode();
        realm.setStatus(status);
        realmMapper.updateById(realm);
        return Boolean.TRUE;
    }

    /**
     * 身份域分页列表
     */
    @Override
    public Page<RealmPageResponse> page(RealmPageRequest realmPageRequest) {
        Page<RealmPageResponse> page = new Page<>(realmPageRequest.getPage(), realmPageRequest.getSize());
        Page<RealmPageResponse> result = realmMapper.page(page, realmPageRequest);
        List<RealmPageResponse> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        List<Long> realmTypeIdList = records.stream().map(RealmPageResponse::getTypeCategoryId).toList();
        List<TypeCategory> typeCategoryList = typeCategoryDomain.findByIdList(realmTypeIdList);
        Map<Long, TypeCategory> typeCategoryMap = typeCategoryList.stream().collect(Collectors.toMap(TypeCategory::getId, e -> e));

        records.forEach(e -> {
            TypeCategory typeCategory = typeCategoryMap.get(e.getTypeCategoryId());
            e.setTypeCategoryName(typeCategory.getName());
            // TODO 登录页处理

            // TODO 认证方式处理
            AuthMethodInfoResponse authMethodInfoResponse = new AuthMethodInfoResponse();
            authMethodInfoResponse.setId(1L);
            authMethodInfoResponse.setName("密码");

            AuthMethodInfoResponse authMethodInfoResponse2 = new AuthMethodInfoResponse();
            authMethodInfoResponse.setId(2L);
            authMethodInfoResponse.setName("短信");

            List<AuthMethodInfoResponse> list = Arrays.asList(authMethodInfoResponse, authMethodInfoResponse2);
            e.setAuthMethodList(list);
        });


        return result;
    }


    public Realm findById(Long id) {
        Realm realm = realmMapper.selectById(id);

        if (realm == null) {
            throw new BizException(REALM_DATA_ERROR);
        }

        return realm;
    }
}




