package com.authsphere.server.app.mapper;

import com.authsphere.server.app.dto.AppPageRequest;
import com.authsphere.server.app.dto.AppPageResponse;
import com.authsphere.server.app.model.App;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author longjiangran
* @description 针对表【app(应用定义表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.App
*/
@Mapper
public interface AppMapper extends BaseMapper<App> {

    /**
     * 获取应用分页数据
     */
    Page<AppPageResponse> page(@Param("page") Page<AppPageResponse> page, @Param("request") AppPageRequest request);
}




