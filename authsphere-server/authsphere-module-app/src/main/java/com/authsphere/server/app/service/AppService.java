package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppPageRequest;
import com.authsphere.server.app.dto.AppRequest;
import com.authsphere.server.app.model.App;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app(应用定义表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppService extends IService<App> {

    /**
     * 分页查询应用定义。
     */
    Page<App> page(AppPageRequest request);

    /**
     * 查询全部应用定义，供下拉选择等轻量场景使用。
     */
    List<App> listAll();

    /**
     * 查询应用详情。
     */
    App detail(Long id);

    /**
     * 新增应用定义。
     */
    Boolean create(AppRequest request);

    /**
     * 编辑应用定义。
     */
    Boolean edit(Long id, AppRequest request);

    /**
     * 启用应用定义。
     */
    Boolean enable(Long id);

    /**
     * 禁用应用定义；存在启用实例时不允许禁用。
     */
    Boolean disable(Long id);
}
