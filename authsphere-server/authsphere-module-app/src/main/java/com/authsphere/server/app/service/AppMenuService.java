package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.model.AppMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_menu(应用菜单资源表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppMenuService extends IService<AppMenu> {

    /**
     * 查询指定应用端的菜单资源全集。
     */
    List<AppMenu> listByClient(Long appClientId);

    /**
     * 查询菜单资源详情。
     */
    AppMenu detail(Long id);

    /**
     * 在指定应用端下新增菜单资源。
     */
    Boolean create(Long appClientId, AppMenuRequest request);

    /**
     * 编辑菜单资源。
     */
    Boolean edit(Long id, AppMenuRequest request);

    /**
     * 启用菜单资源。
     */
    Boolean enable(Long id);

    /**
     * 禁用菜单资源；实例菜单即使启用也不应再参与最终授权。
     */
    Boolean disable(Long id);
}
