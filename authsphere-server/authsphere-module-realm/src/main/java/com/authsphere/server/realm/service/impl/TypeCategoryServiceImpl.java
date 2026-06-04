package com.authsphere.server.realm.service.impl;

import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.convert.TypeCategoryConvert;
import com.authsphere.server.realm.domain.RealmDomain;
import com.authsphere.server.realm.dto.CreateTypeCategoryRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageRequest;
import com.authsphere.server.realm.dto.TypeCategoryPageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.TypeCategoryMapper;
import com.authsphere.server.realm.model.Realm;
import com.authsphere.server.realm.model.TypeCategory;
import com.authsphere.server.realm.service.TypeCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.authsphere.server.realm.error.TypeCategoryErrorCode.TYPE_CATEGORY_BIND_REALM;

/**
 * @author longjiangran
 * @description 针对表【type_category(类型项)】的数据库操作Service实现
 * @createDate 2026-05-28 06:04:57
 */
@Service
@RequiredArgsConstructor
public class TypeCategoryServiceImpl extends ServiceImpl<TypeCategoryMapper, TypeCategory>
        implements TypeCategoryService {

    private final RealmDomain realmDomain;
    private final TypeCategoryMapper typeCategoryMapper;

    @Override
    public Page<TypeCategoryPageResponse> page(TypeCategoryPageRequest request) {
        Page<TypeCategoryPageResponse> page = new Page<>(request.getPage(), request.getSize());
        return typeCategoryMapper.page(page, request);
    }

    @Override
    public List<TypeCategoryPageResponse> listAll() {
        return typeCategoryMapper.listAll();
    }

    /**
     * 提交分类项信息
     *
     */
    @Override
    public Boolean create(CreateTypeCategoryRequest request) {
        checkCodeExists(null, request.getCode());
        TypeCategory typeCategory = TypeCategoryConvert.INSTANCE.model(request);
        typeCategory.setStatus(StatusEnum.NORMAL.getCode());
        typeCategoryMapper.insert(typeCategory);
        return Boolean.TRUE;
    }

    /**
     * 修改分类项信息
     *
     * @param id 分类项Id
     */
    @Override
    public Boolean edit(Long id, CreateTypeCategoryRequest request) {
        TypeCategory typeCategory = findById(id);
        checkCodeExists(id, request.getCode());
        TypeCategoryConvert.INSTANCE.copyToModel(request, typeCategory);
        typeCategoryMapper.updateById(typeCategory);
        return Boolean.TRUE;
    }

    /**
     * 修改分类项状态
     *
     * @param id 分类项Id
     */
    @Override
    public Boolean editStatus(Long id) {
        TypeCategory typeCategory = findById(id);
        List<Realm> list = realmDomain.findListByType(typeCategory.getId());
        typeCategory.setStatus(StatusEnum.reverseStatus(typeCategory.getStatus()));
        if (!CollectionUtils.isEmpty(list) && typeCategory.getStatus().equals(StatusEnum.DISABLED.getCode())) {
            throw new BizException(TYPE_CATEGORY_BIND_REALM);
        }
        typeCategoryMapper.updateById(typeCategory);
        return Boolean.TRUE;
    }

    private TypeCategory findById(Long id) {
        TypeCategory typeCategory = typeCategoryMapper.selectById(id);
        if (typeCategory == null) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_DATA_ERROR);
        }
        return typeCategory;
    }

    private void checkCodeExists(Long currentId, String code) {
        LambdaQueryWrapper<TypeCategory> wrapper = new LambdaQueryWrapper<TypeCategory>()
                .eq(TypeCategory::getCode, code);
        if (currentId != null) {
            wrapper.ne(TypeCategory::getId, currentId);
        }
        if (typeCategoryMapper.selectCount(wrapper) > 0) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_CODE_EXISTS);
        }
    }
}



