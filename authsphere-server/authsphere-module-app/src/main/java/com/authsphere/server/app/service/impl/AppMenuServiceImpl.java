package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.convert.AppMenuConvert;
import com.authsphere.server.app.domain.AppClientDomain;
import com.authsphere.server.app.domain.AppMenuDomain;
import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppMenu;
import com.authsphere.server.app.service.AppMenuService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.common.utils.TreeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author longjiangran
 * @description 针对表【app_menu(应用菜单资源表)】的数据库操作Service实现
 * @createDate 2026-06-03 11:07:48
 */
@Service
@RequiredArgsConstructor
public class AppMenuServiceImpl extends ServiceImpl<AppMenuMapper, AppMenu> implements AppMenuService {

    private final AppMenuMapper appMenuMapper;
    private final AppClientDomain appClientDomain;
    private final AppMenuDomain appMenuDomain;


    /**
     * 获取菜单列表树
     */
    @Override
    public List<AppMenuResponse> listByClient(Long appClientId) {
        AppClient appClient = appClientDomain.findById(appClientId);
        List<AppMenu> appMenuList = appMenuMapper.selectList(new LambdaQueryWrapper<AppMenu>()
                .eq(AppMenu::getAppId, appClient.getAppId())
                .eq(AppMenu::getClientId, appClient.getId())
                .orderByAsc(AppMenu::getSortNo)
                .orderByAsc(AppMenu::getId));

        List<AppMenuResponse> responses = appMenuList.stream().map(AppMenuConvert.INSTANCE::toMenuResponse).toList();
        return TreeUtils.buildTree(responses, AppMenuResponse::getId, AppMenuResponse::getParentId, AppMenuResponse::setChildren, null);
    }

    @Override
    public AppMenu detail(Long id) {
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
        AppMenu parent = appMenuDomain.validateParent(client.getAppId(), client.getId(), request.getParentId());
        AppMenu appMenu = AppMenuConvert.INSTANCE.toAppMenu(request);
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
        AppMenu menu = appMenuDomain.findById(id);
        appMenuDomain.checkCodeExists(id, menu.getAppId(), menu.getClientId(), request.getMenuCode());
        if (Objects.equals(id, request.getParentId())) {
            throw new BizException(AppErrorCode.APP_MENU_PARENT_ERROR);
        }
        AppMenu parent = appMenuDomain.validateParent(menu.getAppId(), menu.getClientId(), request.getParentId());
        AppMenuConvert.INSTANCE.copyAppMenu(menu, request);
        menu.setSortNo(Objects.requireNonNullElse(request.getSortNo(), 0));
        menu.setVisible(Objects.requireNonNullElse(request.getVisible(), Boolean.TRUE));
        if (!parent.getSourceId().equals(menu.getSourceId())) {
            // TODO 修改下级菜单源id
        }
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppMenu menu = appMenuDomain.findById(id);
        menu.setStatus(StatusEnum.NORMAL.getCode());
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppMenu menu = appMenuDomain.findById(id);
        menu.setStatus(StatusEnum.DISABLED.getCode());
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }


}
