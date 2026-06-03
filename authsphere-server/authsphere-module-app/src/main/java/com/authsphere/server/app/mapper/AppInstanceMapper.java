package com.authsphere.server.app.mapper;

import com.authsphere.server.app.dto.AppInstancePageRequest;
import com.authsphere.server.app.model.AppInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author longjiangran
* @description 针对表【app_instance(应用实例表)】的数据库操作Mapper
* @createDate 2026-06-03 11:07:48
* @Entity com.authsphere.server.app.model.AppInstance
*/
@Mapper
public interface AppInstanceMapper extends BaseMapper<AppInstance> {

    /**
     * 应用实例分页查询。
     */
    Page<AppInstance> page(@Param("page") IPage<AppInstance> page,
                           @Param("request") AppInstancePageRequest request);
}




