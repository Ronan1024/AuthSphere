package com.authsphere.server.realm.domain;

import com.authsphere.server.api.realm.RealmApi;
import com.authsphere.server.api.model.dto.realm.RealmInfoResponse;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.common.utils.Assert;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.dto.RealmPageRequest;
import com.authsphere.server.realm.dto.RealmPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.AuthMethodMapper;
import com.authsphere.server.realm.mapper.RealmAuthMethodRelMapper;
import com.authsphere.server.realm.mapper.RealmMapper;
import com.authsphere.server.realm.model.AuthMethod;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.RealmAuthMethodRel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

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
    private final RealmAuthMethodRelMapper realmAuthMethodRelMapper;
    private final AuthMethodMapper authMethodMapper;

    /**
     * 获取身份域信息
     */
    public Realm findById(Long id) {
        return realmMapper.selectById(id);
    }


    /**
     * 根据类型ID获取身份域列表
     */
    public List<Realm> findListByType(List<Long> typeId) {
        return realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .in(Realm::getRealmTypeId, typeId));
    }

    /**
     * 检测 code 是否存在
     */
    public void checkCodeExists(String code) {
        List<Realm> realms = realmMapper.selectList(new LambdaQueryWrapper<Realm>()
                .eq(Realm::getCode, code));
        Assert.notEmpty(realms, BizException.supplier(RealmErrorCode.REALM_CODE_EXISTS));
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
        List<RealmAuthMethodRel> realmAuthMethodRelList = realmAuthMethodRelMapper.selectList(new LambdaQueryWrapper<RealmAuthMethodRel>().eq(RealmAuthMethodRel::getRealmId, realmId));
        List<Long> authMethodIdList = realmAuthMethodRelList.stream().map(RealmAuthMethodRel::getAuthMethodId).toList();
        List<AuthMethod> authMethods = authMethodMapper.selectByIds(authMethodIdList);
        List<String> authMethodCodes = authMethods.stream().map(AuthMethod::getCode).toList();
        RealmInfoResponse realmInfoResponse = RealmConvert.INSTANCE.toRealmInfoResponse(byId);
        realmInfoResponse.setAuthMethod(authMethodCodes);
        return realmInfoResponse;
    }

    /**
     * 获取身份域信息
     */
    @Override
    public List<RealmInfoResponse> list(List<Long> realmIdList) {
        List<Realm> realms = realmMapper.selectByIds(realmIdList);
        return RealmConvert.INSTANCE.toRealmInfoResponse(realms);
    }

    /**
     * 保存或更新身份域信息
     */
    public Boolean createOrUpdateRealm(Realm realm, List<Long> authMethodIds) {
        realmMapper.insertOrUpdate(realm);
        realmAuthMethodRelMapper.delete(new LambdaQueryWrapper<RealmAuthMethodRel>().eq(RealmAuthMethodRel::getRealmId, realm.getId()));
        for (Long authMethodId : authMethodIds.stream().filter(Objects::nonNull).distinct().toList()) {
            RealmAuthMethodRel relation = new RealmAuthMethodRel();
            relation.setRealmId(realm.getId());
            relation.setAuthMethodId(authMethodId);
            realmAuthMethodRelMapper.insert(relation);
        }
        return Boolean.TRUE;
    }

    public void updateRealm(Realm realm) {
        realmMapper.updateById(realm);
    }

    public Page<RealmPageResponse> page(RealmPageRequest realmPageRequest) {
        Page<RealmPageResponse> page = new Page<>(realmPageRequest.getPage(), realmPageRequest.getSize());
        return realmMapper.page(page, realmPageRequest);
    }
}
