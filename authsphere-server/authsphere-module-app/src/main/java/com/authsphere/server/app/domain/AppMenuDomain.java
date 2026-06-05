package com.authsphere.server.app.domain;

import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppMenu;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Service
@RequiredArgsConstructor
public class AppMenuDomain {

    private final AppMenuMapper appMenuMapper;

    public Integer appMenuCount(Long appClientId) {
        return appMenuMapper.selectCount(new LambdaQueryWrapper<AppMenu>()).intValue();
    }


    /**
     * 校验 菜单编号是否存在
     */
    public void checkCodeExists(Long currentId, Long appId, Long clientId, String menuCode) {
        Long count = appMenuMapper.selectCount(new LambdaQueryWrapper<AppMenu>()
                .eq(AppMenu::getAppId, appId)
                .eq(AppMenu::getClientId, clientId)
                .eq(AppMenu::getMenuCode, menuCode)
                .ne(!ObjectUtils.isEmpty(currentId), AppMenu::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_MENU_CODE_EXISTS);
        }
    }


    /**
     * 校验上级菜单是否存在
     */
    public AppMenu validateParent(Long appId, Long clientId, Long parentId) {
        if(ObjectUtils.isEmpty(parentId)){
            return null;
        }

        AppMenu parent = findById(parentId);
        if (!Objects.equals(parent.getAppId(), appId) || !Objects.equals(parent.getClientId(), clientId)) {
            throw new BizException(AppErrorCode.APP_MENU_PARENT_ERROR);
        }

        return parent;
    }


    public AppMenu findById(Long id) {
        AppMenu menu = appMenuMapper.selectById(id);
        if (ObjectUtils.isEmpty(menu)) {
            throw new BizException(AppErrorCode.APP_MENU_DATA_ERROR);
        }
        return menu;
    }
}

