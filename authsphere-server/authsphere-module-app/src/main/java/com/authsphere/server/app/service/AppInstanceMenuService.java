package com.authsphere.server.app.service;

import com.authsphere.server.app.model.AppInstanceMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_instance_menu(应用实例启用菜单表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppInstanceMenuService extends IService<AppInstanceMenu> {

    /**
     * 查询应用实例已配置的可用菜单。
     */
    List<AppInstanceMenu> listByInstance(Long instanceId);

    /**
     * 从应用菜单资源全集同步到实例菜单配置，已存在配置只保留并刷新为启用状态。
     */
    Boolean syncFromApp(Long instanceId);

    /**
     * 启用实例下指定菜单。
     */
    Boolean enable(Long instanceId, Long menuId);

    /**
     * 禁用实例下指定菜单。
     */
    Boolean disable(Long instanceId, Long menuId);
}
