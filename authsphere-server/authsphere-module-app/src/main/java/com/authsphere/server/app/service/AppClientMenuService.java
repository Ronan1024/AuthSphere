package com.authsphere.server.app.service;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.common.enums.StatusEnum;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author longjiangran
* @description 针对表【app_menu(应用菜单资源表)】的数据库操作Service
* @createDate 2026-06-03 11:07:48
*/
public interface AppClientMenuService extends IService<AppClientMenu> {

    /**
     * 查询指定应用端的菜单资源全集。
     */
    List<AppMenuResponse> listByClient(Long appClientId);

    /**
     * 查询菜单资源详情。
     */
    AppClientMenu detail(Long id);

    /**
     * 在指定应用端下新增菜单资源。
     */
    @Transactional(rollbackFor = Exception.class)
    Boolean create(Long appClientId, AppMenuRequest request);

    /**
     * 编辑菜单资源。
     */
    @Transactional(rollbackFor = Exception.class)
    Boolean edit(Long id, AppMenuRequest request);


    /**
     * 修改菜单状态
     */
    Boolean changeMenuStatus(Long id, StatusEnum status);

    /**
     * 删除指定菜单
     */
    Boolean delete(Long menuId);
}
