package com.authsphere.server.role.service.impl;

import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.mapper.AppPermissionMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientMenu;
import com.authsphere.server.app.model.AppClientPermission;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.role.dto.AccountRoleAssignRequest;
import com.authsphere.server.role.dto.RoleRequest;
import com.authsphere.server.role.dto.RoleResourceAssignRequest;
import com.authsphere.server.role.mapper.AccountRoleMapper;
import com.authsphere.server.role.mapper.RoleMapper;
import com.authsphere.server.role.mapper.RoleResourceMapper;
import com.authsphere.server.role.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleResourceMapper roleResourceMapper;
    @Mock
    private AccountRoleMapper accountRoleMapper;
    @Mock
    private AppClientMapper appClientMapper;
    @Mock
    private AppMenuMapper appMenuMapper;
    @Mock
    private AppPermissionMapper appPermissionMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(
                roleMapper,
                roleResourceMapper,
                accountRoleMapper,
                appClientMapper,
                appMenuMapper,
                appPermissionMapper
        );
    }

    @Test
    void createShouldRejectDuplicateRoleCodeInSameClientScope() {
        AppClient client = enabledClient(10L);
        when(appClientMapper.selectById(10L)).thenReturn(client);
        when(roleMapper.selectCount(any())).thenReturn(1L);

        RoleRequest request = new RoleRequest();
        request.setRealmId(1L);
        request.setClientId(10L);
        request.setRoleName("运营");
        request.setRoleCode("operator");

        assertThrows(BizException.class, () -> roleService.create(request));
    }

    @Test
    void assignResourcesShouldRejectResourcesOutsideRoleClient() {
        Role role = enabledRole(1L, 10L);
        when(roleMapper.selectById(1L)).thenReturn(role);

        AppClientMenu foreignMenu = new AppClientMenu();
        foreignMenu.setId(100L);
        foreignMenu.setClientId(99L);
        foreignMenu.setStatus(StatusEnum.NORMAL.getCode());
        when(appMenuMapper.selectBatchIds(ArgumentMatchers.<List<Long>>any())).thenReturn(List.of(foreignMenu));
        when(appPermissionMapper.selectBatchIds(ArgumentMatchers.<List<Long>>any())).thenReturn(List.of());

        RoleResourceAssignRequest request = new RoleResourceAssignRequest();
        request.setResourceIds(List.of(100L));

        assertThrows(BizException.class, () -> roleService.assignResources(1L, request));
    }

    @Test
    void assignAccountRolesShouldRejectRolesOutsideClient() {
        AppClient client = enabledClient(10L);
        when(appClientMapper.selectById(10L)).thenReturn(client);

        Role foreignRole = enabledRole(2L, 99L);
        when(roleMapper.selectBatchIds(ArgumentMatchers.<List<Long>>any())).thenReturn(List.of(foreignRole));

        AccountRoleAssignRequest request = new AccountRoleAssignRequest();
        request.setClientId(10L);
        request.setRoleIds(List.of(2L));

        assertThrows(BizException.class, () -> roleService.assignAccountRoles(1000L, request));
    }

    private AppClient enabledClient(Long id) {
        AppClient client = new AppClient();
        client.setId(id);
        client.setStatus(StatusEnum.NORMAL.getCode());
        return client;
    }

    private Role enabledRole(Long id, Long clientId) {
        Role role = new Role();
        role.setId(id);
        role.setClientId(clientId);
        role.setStatus(StatusEnum.NORMAL.getCode());
        return role;
    }
}
