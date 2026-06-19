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
        Page<RealmTypePageResponse> page = new Page<>(request.getPage(), request.getSize());
        return realmTypeMapper.page(page, request);
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
        List<Realm> refs = realmDomain.findListByType(id);
        int disabledCount = CollectionUtils.isEmpty(refs) ? 0 : Math.toIntExact(refs.stream().filter(realm -> DISABLED.getCode().equals(realm.getStatus())).count());
        response.setDisabledCount(disabledCount);
        response.setRealmList(RealmConvert.INSTANCE.toRealmListResponse(refs));
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

        List<Realm> list = realmDomain.findListByType(typeCategory.getId());
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

