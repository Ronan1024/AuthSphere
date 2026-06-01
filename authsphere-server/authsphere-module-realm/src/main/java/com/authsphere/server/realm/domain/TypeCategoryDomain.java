package com.authsphere.server.realm.domain;

import com.authsphere.server.realm.mapper.TypeCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
