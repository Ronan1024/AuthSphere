package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.MfaPolicyPageRequest;
import com.authsphere.server.realm.dto.MfaPolicyResponse;
import com.authsphere.server.realm.model.MfaPolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MFA 策略模板 Mapper。
 */
@Mapper
public interface MfaPolicyMapper extends BaseMapper<MfaPolicy> {

    Page<MfaPolicyResponse> page(@Param("page") IPage<MfaPolicyResponse> page,
                                 @Param("request") MfaPolicyPageRequest request);
}
