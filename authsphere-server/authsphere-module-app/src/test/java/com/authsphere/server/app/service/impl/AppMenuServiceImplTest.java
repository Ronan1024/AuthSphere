package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.domain.AppClientDomain;
import com.authsphere.server.app.domain.AppMenuDomain;
import com.authsphere.server.app.dto.AppMenuResponse;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientMenu;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 应用菜单服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppMenuServiceImplTest {

    @Mock
    private AppMenuMapper appMenuMapper;

    @Mock
    private AppClientDomain appClientDomain;

    @Mock
    private AppMenuDomain appMenuDomain;

    @InjectMocks
    private AppClientMenuServiceImpl appMenuService;

    @Test
    void listByClientShouldReturnMenuTree() {
        AppClient client = new AppClient();
        client.setId(10L);
        client.setAppId(20L);
        when(appClientDomain.findById(10L)).thenReturn(client);
        when(appMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                createMenu(1L, null, "系统管理", 10),
                createMenu(2L, 1L, "账号管理", 20),
                createMenu(3L, 2L, "新增账号", 30),
                createMenu(4L, null, "工作台", 40)
        ));

        List<AppMenuResponse> tree = appMenuService.listByClient(10L);

        assertEquals(List.of("系统管理", "工作台"), tree.stream().map(AppMenuResponse::getMenuName).toList());
        assertEquals(List.of("账号管理"), tree.getFirst().getChildren().stream()
                .map(AppMenuResponse::getMenuName)
                .toList());
        assertEquals(List.of("新增账号"), tree.getFirst().getChildren().getFirst().getChildren().stream()
                .map(AppMenuResponse::getMenuName)
                .toList());
    }

    private AppClientMenu createMenu(Long id, Long parentId, String menuName, Integer sortNo) {
        AppClientMenu menu = new AppClientMenu();
        menu.setId(id);
        menu.setAppId(20L);
        menu.setClientId(10L);
        menu.setParentId(parentId);
        menu.setMenuCode("menu_" + id);
        menu.setMenuName(menuName);
        menu.setSortNo(sortNo);
        return menu;
    }
}
