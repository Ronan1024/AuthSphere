package com.authsphere.server.app.domain;

import com.authsphere.server.app.mapper.AppMenuMapper;
import com.authsphere.server.app.model.AppMenu;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}

