package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.convert.AppMenuConvert;
import com.authsphere.server.app.domain.AppClientDomain;
import com.authsphere.server.app.domain.AppMenuDomain;
import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.service.AppClientMenuService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.common.utils.TreeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author longjiangran
 * @description 针对表【app_menu(应用菜单资源表)】的数据库操作Service实现
 * @createDate 2026-06-03 11:07:48
 */
@Service
@RequiredArgsConstructor
public class AppClientMenuServiceImpl extends ServiceImpl<AppMenuMapper, AppClientMenu> implements AppClientMenuService {

    private final AppMenuMapper appMenuMapper;
    private final AppClientDomain appClientDomain;
    private final AppMenuDomain appMenuDomain;


    /**
     * 获取菜单列表树
     */
    @Override
    public List<AppMenuResponse> listByClient(Long appClientId) {
        AppClient appClient = appClientDomain.findById(appClientId);
        List<AppClientMenu> appMenuList = appMenuMapper.selectList(new LambdaQueryWrapper<AppClientMenu>()
                .eq(AppClientMenu::getAppId, appClient.getAppId())
                .eq(AppClientMenu::getClientId, appClient.getId())
                .orderByAsc(AppClientMenu::getSortNo)
                .orderByAsc(AppClientMenu::getId));

        List<AppMenuResponse> responses = appMenuList.stream().map(AppMenuConvert.INSTANCE::toMenuResponse).toList();
        return TreeUtils.buildTree(responses, AppMenuResponse::getId, AppMenuResponse::getParentId, AppMenuResponse::setChildren, null);
    }

    @Override
    public AppClientMenu detail(Long id) {
        return appMenuDomain.findById(id);
    }


    /**
     * 创建菜单资源
     *
     * @param appClientId 应用客户端
     */
    @Override
    public Boolean create(Long appClientId, AppMenuRequest request) {
        AppClient client = appClientDomain.findById(appClientId);
        appMenuDomain.checkCodeExists(null, client.getAppId(), client.getId(), request.getMenuCode());
        AppClientMenu parent = appMenuDomain.validateParent(client.getAppId(), client.getId(), request.getParentId());
        AppClientMenu appMenu = AppMenuConvert.INSTANCE.toAppMenu(request);
        appMenu.setAppId(client.getAppId());
        appMenu.setClientId(client.getId());
        appMenu.setSortNo(Objects.requireNonNullElse(request.getSortNo(), 0));
        appMenu.setVisible(Objects.requireNonNullElse(request.getVisible(), Boolean.TRUE));
        appMenuMapper.insert(appMenu);
        if (appMenu.getParentId().equals(0L) || ObjectUtils.isEmpty(appMenu.getParentId())) {
            appMenu.setSourceId(appMenu.getId());
        } else {
            appMenu.setSourceId(parent.getSourceId());
        }
        appMenuMapper.updateById(appMenu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppMenuRequest request) {
        AppClientMenu menu = appMenuDomain.findById(id);
        appMenuDomain.checkCodeExists(id, menu.getAppId(), menu.getClientId(), request.getMenuCode());
        if (Objects.equals(id, request.getParentId())) {
            throw new BizException(AppErrorCode.APP_MENU_PARENT_ERROR);
        }
        AppClientMenu parent = appMenuDomain.validateParent(menu.getAppId(), menu.getClientId(), request.getParentId());
        AppMenuConvert.INSTANCE.copyAppMenu(menu, request);
        menu.setSortNo(Objects.requireNonNullElse(request.getSortNo(), 0));
        menu.setVisible(Objects.requireNonNullElse(request.getVisible(), Boolean.TRUE));
        appMenuMapper.updateById(menu);
        if (!ObjectUtils.isEmpty(parent) && !parent.getSourceId().equals(menu.getSourceId())) {
            // 调整下级菜单源id
            List<AppClientMenu> appMenuList = appMenuMapper.selectList(new LambdaQueryWrapper<AppClientMenu>()
                    .eq(AppClientMenu::getSourceId, parent.getSourceId())
                    .ne(AppClientMenu::getId, id));
            List<AppClientMenu> menuList = TreeUtils.findSubTree(appMenuList, AppClientMenu::getId, AppClientMenu::getParentId, null, menu.getId());
            List<Long> menuIdList = menuList.stream().map(AppClientMenu::getId).collect(Collectors.toList());
            menuIdList.add(menu.getId());
            appMenuMapper.update(new LambdaUpdateWrapper<AppClientMenu>()
                    .in(AppClientMenu::getId, menuIdList)
                    .set(AppClientMenu::getSourceId, parent.getSourceId()));

        }
        return Boolean.TRUE;
    }

    /**
     * 修改菜单状态
     */
    @Override
    public Boolean changeMenuStatus(Long id, StatusEnum status) {
        AppClientMenu menu = appMenuDomain.findById(id);
        List<AppClientMenu> listBySource = appMenuDomain.findListBySource(menu.getSourceId());
        List<AppClientMenu> menuList = TreeUtils.findSubTree(listBySource, AppClientMenu::getId, AppClientMenu::getParentId, null, menu.getId());
        List<Long> menuIdList = menuList.stream().map(AppClientMenu::getId).collect(Collectors.toList());
        menuIdList.add(menu.getId());
        appMenuMapper.update(new LambdaUpdateWrapper<AppClientMenu>()
                .in(AppClientMenu::getId, menuIdList)
                .set(AppClientMenu::getStatus, status.getCode()));
        return Boolean.TRUE;
    }

    /**
     * 删除指定菜单
     *
     */
    @Override
    public Boolean delete(Long menuId) {

        AppClientMenu appClientMenu = appMenuDomain.findById(menuId);

        Long count = appMenuMapper.selectCount(new LambdaQueryWrapper<AppClientMenu>()
                .eq(AppClientMenu::getParentId, appClientMenu.getId()));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_MENU_DELETE_ERROR);
        }

        return appMenuMapper.deleteById(menuId) > 0;
    }


}
