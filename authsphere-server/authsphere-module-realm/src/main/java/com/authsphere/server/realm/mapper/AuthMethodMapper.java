package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.AuthMethodOptionResponse;
import com.authsphere.server.realm.dto.AuthMethodPageRequest;
import com.authsphere.server.realm.dto.AuthMethodReferenceResponse;
import com.authsphere.server.realm.dto.AuthMethodResponse;
import com.authsphere.server.realm.model.AuthMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证方式Mapper。
 */
@Mapper
public interface AuthMethodMapper extends BaseMapper<AuthMethod> {

    Page<AuthMethodResponse> page(@Param("page") IPage<AuthMethodResponse> page,
                                  @Param("request") AuthMethodPageRequest request);

    List<AuthMethodOptionResponse> listEnabled(@Param("position") String position);

    List<AuthMethodReferenceResponse> listReferences(@Param("code") String code);

    Integer countReferences(@Param("code") String code);

    Integer countByCode(@Param("code") String code, @Param("currentId") Long currentId);
}
