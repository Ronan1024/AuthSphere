package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppClientMenu;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppInstanceMenu;
import com.authsphere.server.app.service.AppInstanceMenuService;
import com.authsphere.server.app.mapper.AppInstanceMenuMapper;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
* @author longjiangran
* @description 针对表【app_instance_menu(应用实例启用菜单表)】的数据库操作Service实现
* @createDate 2026-06-03 11:07:48
*/
@Service
@RequiredArgsConstructor
public class AppInstanceMenuServiceImpl extends ServiceImpl<AppInstanceMenuMapper, AppInstanceMenu> implements AppInstanceMenuService {

    private final AppInstanceMenuMapper appInstanceMenuMapper;
    private final AppInstanceMapper appInstanceMapper;
    private final AppMenuMapper appMenuMapper;

    @Override
    public List<AppInstanceMenu> listByInstance(Long instanceId) {
        ensureInstance(instanceId);
        return appInstanceMenuMapper.selectList(new LambdaQueryWrapper<AppInstanceMenu>()
                .eq(AppInstanceMenu::getAppInstanceId, instanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncFromApp(Long instanceId) {
        AppInstance instance = ensureInstance(instanceId);
        List<AppClientMenu> menus = appMenuMapper.selectList(new LambdaQueryWrapper<AppClientMenu>()
                .eq(AppClientMenu::getAppId, instance.getAppId()));
        for (AppClientMenu menu : menus) {
            upsertStatus(instanceId, menu.getId(), StatusEnum.NORMAL.getCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long instanceId, Long menuId) {
        validateInstanceMenu(instanceId, menuId);
        upsertStatus(instanceId, menuId, StatusEnum.NORMAL.getCode());
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long instanceId, Long menuId) {
        validateInstanceMenu(instanceId, menuId);
        upsertStatus(instanceId, menuId, StatusEnum.DISABLED.getCode());
        return Boolean.TRUE;
    }

    private void upsertStatus(Long instanceId, Long menuId, Integer status) {
        AppInstanceMenu relation = appInstanceMenuMapper.selectOne(new LambdaQueryWrapper<AppInstanceMenu>()
                .eq(AppInstanceMenu::getAppInstanceId, instanceId)
                .eq(AppInstanceMenu::getMenuId, menuId));
        if (relation == null) {
            relation = new AppInstanceMenu();
            relation.setAppInstanceId(instanceId);
            relation.setMenuId(menuId);
            relation.setStatus(status);
            appInstanceMenuMapper.insert(relation);
        } else {
            relation.setStatus(status);
            appInstanceMenuMapper.updateById(relation);
        }
    }

    private void validateInstanceMenu(Long instanceId, Long menuId) {
        AppInstance instance = ensureInstance(instanceId);
        AppClientMenu menu = appMenuMapper.selectById(menuId);
        if (menu == null || !Objects.equals(menu.getAppId(), instance.getAppId())) {
            throw new BizException(AppErrorCode.APP_INSTANCE_RESOURCE_ERROR);
        }
    }

    private AppInstance ensureInstance(Long instanceId) {
        AppInstance instance = appInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DATA_ERROR);
        }
        return instance;
    }

}

