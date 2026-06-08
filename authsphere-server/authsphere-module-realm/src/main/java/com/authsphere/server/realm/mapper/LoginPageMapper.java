package com.authsphere.server.realm.mapper;

import com.authsphere.server.realm.dto.LoginPagePageRequest;
import com.authsphere.server.realm.dto.LoginPageOptionResponse;
import com.authsphere.server.realm.dto.LoginPageResponse;
import com.authsphere.server.realm.model.LoginPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录页 Mapper。
 */
@Mapper
public interface LoginPageMapper extends BaseMapper<LoginPage> {

    /**
     * 分页查询登录页列表及引用总数。
     *
     * @param page MyBatis-Plus分页参数
     * @param request 登录页查询条件
     * @return 登录页分页数据
     */
    Page<LoginPageResponse> page(@Param("page") IPage<LoginPageResponse> page,
                                 @Param("request") LoginPagePageRequest request);

    /**
     * 查询启用状态的登录页选择项。
     *
     * @return 登录页选择项
     */
    List<LoginPageOptionResponse> listEnabled();

    /**
     * 统计引用指定登录页的身份域数量。
     *
     * @param loginPageId 登录页ID
     * @return 身份域引用数量
     */
    Integer countRealmReferences(@Param("loginPageId") Long loginPageId);

    /**
     * 统计引用指定登录页的客户端数量。
     *
     * @param loginPageId 登录页ID
     * @return 客户端引用数量
     */
    Integer countClientReferences(@Param("loginPageId") Long loginPageId);

    /**
     * 统计重复编码数量，编辑时排除当前登录页。
     *
     * @param code 登录页编码
     * @param currentId 当前登录页ID，新增时为空
     * @return 重复编码数量
     */
    Integer countByCode(@Param("code") String code, @Param("currentId") Long currentId);
}
