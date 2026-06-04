package com.authsphere.server.app.domain;

import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.model.AppClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Service
@RequiredArgsConstructor
public class AppClientDomain {

    private final AppClientMapper appClientMapper;

    /**
     * 保存应用客户端
     *
     */
    public Boolean saveClient(List<AppClient> appClients) {
        appClientMapper.insert(appClients);

        return Boolean.TRUE;
    }


    /**
     * 根据应用 id 获取客户端信息
     * @param appId 应用id集合
     */
    public List<AppClient> listClients(List<Long> appId) {
        return appClientMapper.selectList(new LambdaQueryWrapper<AppClient>().in(AppClient::getAppId, appId));
    }




}
