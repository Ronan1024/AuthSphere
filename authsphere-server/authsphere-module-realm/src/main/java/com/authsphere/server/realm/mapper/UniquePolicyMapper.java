package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.UniquePolicyPageRequest;
import com.authsphere.server.realm.dto.UniquePolicyResponse;
import com.authsphere.server.realm.model.UniquePolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 账号唯一性规则模板 Mapper。
 */
@Mapper
public interface UniquePolicyMapper extends BaseMapper<UniquePolicy> {

    Page<UniquePolicyResponse> page(@Param("page") IPage<UniquePolicyResponse> page,
                                    @Param("request") UniquePolicyPageRequest request);
}
