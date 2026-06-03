package com.authsphere.server.app.mapper;

import com.authsphere.server.app.dto.AppClientInstancePageRequest;
import com.authsphere.server.app.model.AppClientInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 应用端实例 Mapper。
 */
@Mapper
public interface AppClientInstanceMapper extends BaseMapper<AppClientInstance> {

    /**
     * 应用端实例分页查询。
     */
    Page<AppClientInstance> page(@Param("page") IPage<AppClientInstance> page,
                                 @Param("request") AppClientInstancePageRequest request);
}
