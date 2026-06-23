package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.RealmConvert;
import com.authsphere.server.realm.convert.RealmTypeConvert;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.domain.RealmTypeDomain;
import com.authsphere.server.realm.dto.CreateRealmTypeRequest;
import com.authsphere.server.realm.dto.RealmTypeInfoResponse;
import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.error.RealmTypeErrorCode;
import com.authsphere.server.realm.mapper.RealmTypeMapper;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.RealmType;
import com.authsphere.server.realm.service.RealmTypeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.authsphere.server.realm.error.RealmTypeErrorCode.TYPE_CATEGORY_BIND_REALM;
import static com.authsphere.server.common.enums.StatusEnum.DISABLED;

/**
 * 身份域类型业务处理
 */
@Service
@RequiredArgsConstructor
public class RealmTypeServiceImpl extends ServiceImpl<RealmTypeMapper, RealmType> implements RealmTypeService {

    private final RealmDomain realmDomain;
    private final RealmTypeDomain realmTypeDomain;
    private final RealmTypeMapper realmTypeMapper;

    @Override
    public Page<RealmTypePageResponse> page(RealmTypePageRequest request) {
        Page<RealmTypePageResponse> result = realmTypeDomain.page(request);
        List<RealmTypePageResponse> records = result.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> realmTypeIdList = records.stream().map(RealmTypePageResponse::getId).distinct().toList();
            List<Realm> realmList = realmDomain.findListByType(realmTypeIdList);
            Map<Long, List<Realm>> realmGroupMap = realmList.stream().collect(Collectors.groupingBy(Realm::getRealmTypeId));
            records.forEach(e -> e.setRealmCount(realmGroupMap.getOrDefault(e.getId(), Collections.emptyList()).size()));
        }
        return result;
    }

    @Override
    public List<RealmTypePageResponse> listAll() {
        return realmTypeMapper.listAll();
    }

    /**
     * 获取身份域详情
     *
     * @param id 身份域类型 ID
     */
    @Override
    public RealmTypeInfoResponse detail(Long id) {
        RealmType realmType = findById(id);
        RealmTypeInfoResponse response = RealmTypeConvert.INSTANCE.infoResponse(realmType);
        List<Realm> referencedRealmList = realmDomain.findListByType(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(referencedRealmList)) {
            response.setRealmCount(0);
            response.setEnabledCount(0);
            response.setDisabledCount(0);
            response.setRealmList(Collections.emptyList());
        } else {
            long disabledCount = referencedRealmList.stream().filter(realm -> DISABLED.getCode().equals(realm.getStatus())).count();
            int totalCount = referencedRealmList.size();
            response.setRealmCount(totalCount);
            response.setEnabledCount(Math.toIntExact(totalCount - disabledCount));
            response.setDisabledCount(Math.toIntExact(disabledCount));
            List<Realm> realms = referencedRealmList.stream().sorted(Comparator.comparing((Realm realm) -> DISABLED.getCode().equals(realm.getStatus()))
                    .thenComparing(Realm::getName, Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(Realm::getCode, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
            response.setRealmList(RealmConvert.INSTANCE.toRealmListResponse(realms));
        }


        return response;
    }

    /**
     * 提交分类项信息
     *
     */
    @Override
    public Boolean create(CreateRealmTypeRequest request) {
        realmTypeDomain.checkCodeExists(null, request.getCode());
        RealmType typeCategory = RealmTypeConvert.INSTANCE.model(request);
        typeCategory.setStatus(StatusEnum.NORMAL.getCode());
        realmTypeMapper.insert(typeCategory);
        return Boolean.TRUE;
    }

    /**
     * 修改分类项信息
     *
     * @param id 分类项Id
     */
    @Override
    public Boolean edit(Long id, CreateRealmTypeRequest request) {
        RealmType realmType = findById(id);
        realmTypeDomain.checkCodeExists(id, request.getCode());
        realmTypeDomain.checkCanEdit(realmType);

        RealmTypeConvert.INSTANCE.copyToModel(request, realmType);
        realmTypeMapper.updateById(realmType);
        return Boolean.TRUE;
    }

    /**
     * 修改分类项状态
     *
     * @param id 分类项Id
     */
    @Override
    public Boolean editStatus(Long id) {
        RealmType typeCategory = findById(id);
        typeCategory.setStatus(StatusEnum.reverseStatus(typeCategory.getStatus()));
        realmTypeMapper.updateById(typeCategory);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        RealmType typeCategory = findById(id);

        if (typeCategory.getSystemBuiltin()) {
            throw new BizException(RealmTypeErrorCode.TYPE_CATEGORY_SYSTEM_BUILTIN);
        }

        List<Realm> list = realmDomain.findListByType(Collections.singletonList(typeCategory.getId()));
        if (!CollectionUtils.isEmpty(list)) {
            throw new BizException(TYPE_CATEGORY_BIND_REALM);
        }
        realmTypeMapper.deleteById(id);
        return Boolean.TRUE;
    }

    private RealmType findById(Long id) {
        RealmType typeCategory = realmTypeMapper.selectById(id);
        if (typeCategory == null) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_DATA_ERROR);
        }
        return typeCategory;
    }
}
