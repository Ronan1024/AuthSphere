package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.PasswordPolicyListResponse;
import com.authsphere.server.realm.dto.PasswordPolicyPageRequest;
import com.authsphere.server.realm.dto.PasswordPolicyResponse;
import com.authsphere.server.realm.model.PasswordPolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 密码策略 Mapper。
 */
@Mapper
public interface PasswordPolicyMapper extends BaseMapper<PasswordPolicy> {

    Page<PasswordPolicyListResponse> page(@Param("page") IPage<PasswordPolicyListResponse> page,
                                          @Param("request") PasswordPolicyPageRequest request);
}
