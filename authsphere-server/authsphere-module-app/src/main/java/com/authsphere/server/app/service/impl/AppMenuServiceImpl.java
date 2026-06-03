package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppMenuRequest;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppMenu;
import com.authsphere.server.app.service.AppMenuService;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
    private final AppClientMapper appClientMapper;

    @Override
    public List<AppMenu> listByClient(Long appClientId) {
        AppClient client = findClient(appClientId);
        return appMenuMapper.selectList(new LambdaQueryWrapper<AppMenu>()
                .eq(AppMenu::getAppCode, client.getAppCode())
                .eq(AppMenu::getClientCode, client.getClientCode())
                .orderByAsc(AppMenu::getSortNo)
                .orderByAsc(AppMenu::getId));
    }

    @Override
    public AppMenu detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(Long appClientId, AppMenuRequest request) {
        AppClient client = findClient(appClientId);
        checkCodeExists(null, client.getAppCode(), client.getClientCode(), request.getMenuCode());
        validateParent(client.getAppCode(), client.getClientCode(), request.getParentId());
        AppMenu menu = new AppMenu();
        BeanUtils.copyProperties(request, menu);
        menu.setAppCode(client.getAppCode());
        menu.setClientCode(client.getClientCode());
        menu.setSortNo(Objects.requireNonNullElse(request.getSortNo(), 0));
        menu.setVisible(Objects.requireNonNullElse(request.getVisible(), 1));
        menu.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appMenuMapper.insert(menu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppMenuRequest request) {
        AppMenu menu = findById(id);
        checkCodeExists(id, menu.getAppCode(), menu.getClientCode(), request.getMenuCode());
        if (Objects.equals(id, request.getParentId())) {
            throw new BizException(AppErrorCode.APP_MENU_PARENT_ERROR);
        }
        validateParent(menu.getAppCode(), menu.getClientCode(), request.getParentId());
        BeanUtils.copyProperties(request, menu, "id", "appCode", "clientCode", "createTime", "updateTime");
        menu.setSortNo(Objects.requireNonNullElse(request.getSortNo(), 0));
        menu.setVisible(Objects.requireNonNullElse(request.getVisible(), 1));
        menu.setBuiltIn(Objects.requireNonNullElse(request.getBuiltIn(), 0));
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppMenu menu = findById(id);
        menu.setStatus(StatusEnum.NORMAL.getCode());
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppMenu menu = findById(id);
        menu.setStatus(StatusEnum.DISABLED.getCode());
        appMenuMapper.updateById(menu);
        return Boolean.TRUE;
    }

    private AppClient findClient(Long appClientId) {
        AppClient client = appClientMapper.selectById(appClientId);
        if (client == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
        return client;
    }

    private AppMenu findById(Long id) {
        AppMenu menu = appMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(AppErrorCode.APP_MENU_DATA_ERROR);
        }
        return menu;
    }

    private void checkCodeExists(Long currentId, String appCode, String clientCode, String menuCode) {
        Long count = appMenuMapper.selectCount(new LambdaQueryWrapper<AppMenu>()
                .eq(AppMenu::getAppCode, appCode)
                .eq(AppMenu::getClientCode, clientCode)
                .eq(AppMenu::getMenuCode, menuCode)
                .ne(currentId != null, AppMenu::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_MENU_CODE_EXISTS);
        }
    }

    private void validateParent(String appCode, String clientCode, Long parentId) {
        if (parentId == null) {
            return;
        }
        AppMenu parent = findById(parentId);
        if (!Objects.equals(parent.getAppCode(), appCode)
                || !Objects.equals(parent.getClientCode(), clientCode)) {
            throw new BizException(AppErrorCode.APP_MENU_PARENT_ERROR);
        }
    }

}

