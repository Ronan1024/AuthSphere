package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.AuthPolicyOptionResponse;
import com.authsphere.server.realm.dto.AuthPolicyPageRequest;
import com.authsphere.server.realm.dto.AuthPolicyReferenceResponse;
import com.authsphere.server.realm.dto.AuthPolicyResponse;
import com.authsphere.server.realm.model.AuthPolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证策略Mapper。
 */
@Mapper
public interface AuthPolicyMapper extends BaseMapper<AuthPolicy> {

    /**
     * 分页查询认证策略及引用总数。
     *
     * @param page 分页参数
     * @param request 策略筛选条件
     * @return 认证策略分页数据
     */
    Page<AuthPolicyResponse> page(@Param("page") IPage<AuthPolicyResponse> page,
                                  @Param("request") AuthPolicyPageRequest request);

    /**
     * 查询启用认证策略选择项。
     *
     * @return 启用状态的认证策略选择项
     */
    List<AuthPolicyOptionResponse> listEnabled();

    /**
     * 查询认证策略的身份域和客户端引用明细。
     *
     * @param authPolicyId 认证策略主键
     * @return 身份域和客户端引用明细
     */
    List<AuthPolicyReferenceResponse> listReferences(@Param("authPolicyId") Long authPolicyId);

    /**
     * 统计身份域引用数量。
     *
     * @param authPolicyId 认证策略主键
     * @return 引用该策略的身份域数量
     */
    Integer countRealmReferences(@Param("authPolicyId") Long authPolicyId);

    /**
     * 统计客户端引用数量。
     *
     * @param authPolicyId 认证策略主键
     * @return 引用该策略的客户端数量
     */
    Integer countClientReferences(@Param("authPolicyId") Long authPolicyId);

    /**
     * 统计重复编码数量，编辑时排除当前策略。
     *
     * @param code 待校验的认证策略编码
     * @param currentId 编辑场景下需要排除的当前策略主键
     * @return 相同编码的认证策略数量
     */
    Integer countByCode(@Param("code") String code, @Param("currentId") Long currentId);
}
