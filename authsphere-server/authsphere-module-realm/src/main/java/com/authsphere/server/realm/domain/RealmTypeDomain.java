package com.authsphere.server.realm.domain;

import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.realm.dto.RealmTypePageRequest;
import com.authsphere.server.realm.dto.RealmTypePageResponse;
import com.authsphere.server.realm.error.RealmErrorCode;
import com.authsphere.server.realm.mapper.RealmTypeMapper;
import com.authsphere.server.realm.model.RealmType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class RealmTypeDomain {

    private final RealmTypeMapper realmTypeMapper;


    public List<RealmType> findByIdList(List<Long> id) {
        return realmTypeMapper.selectByIds(id);
    }


    /**
     * 获取身份域类型
     * @param id 需要查询的身份域ID
     */
    public RealmType findById(Long id) {
        return realmTypeMapper.selectById(id);
    }



    /**
     * 校验是否身份域类型是否存在
     */
    public void checkCodeExists(Long currentId, String code) {
        LambdaQueryWrapper<RealmType> wrapper = new LambdaQueryWrapper<RealmType>().eq(RealmType::getCode, code);
        if (currentId != null) {
            wrapper.ne(RealmType::getId, currentId);
        }
        if (realmTypeMapper.selectCount(wrapper) > 0) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_CODE_EXISTS);
        }
    }


    /**
     * 校验当前身份域类型是否可以进行编辑
     */
    public void checkCanEdit(RealmType realmType) {
        Boolean systemBuiltin = realmType.getSystemBuiltin();
        Boolean editable = realmType.getEditable();
        if (systemBuiltin.equals(Boolean.TRUE) || editable.equals(Boolean.FALSE)) {
            throw new BizException(RealmErrorCode.TYPE_CATEGORY_NOT_EDITABLE);
        }

    }

    public Page<RealmTypePageResponse> page(RealmTypePageRequest request) {
        Page<RealmTypePageResponse> page = new Page<>(request.getPage(), request.getSize());
        return realmTypeMapper.page(page, request);
    }
}
