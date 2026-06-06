package com.authsphere.server.realm.domain;

import com.authsphere.server.realm.mapper.TypeCategoryMapper;
import com.authsphere.server.realm.model.TypeCategory;
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
public class TypeCategoryDomain {

    private final TypeCategoryMapper typeCategoryMapper;




    public List<TypeCategory> findByIdList(List<Long> id) {
        return typeCategoryMapper.selectByIds(id);
    }


}
