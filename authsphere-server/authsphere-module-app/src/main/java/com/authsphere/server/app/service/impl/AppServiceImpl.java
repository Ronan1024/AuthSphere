package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.convert.AppClientConvert;
import com.authsphere.server.app.convert.AppConvert;
import com.authsphere.server.app.domain.AppClientDomain;
import com.authsphere.server.app.domain.AppDomain;
import com.authsphere.server.app.dto.*;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.authsphere.server.app.mapper.AppMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.app.service.AppClientService;
import com.authsphere.server.app.service.AppService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author longjiangran
 * @description 针对表【app(应用定义表)】的数据库操作Service实现
 * @createDate 2026-06-03 11:07:48
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final AppMapper appMapper;
    private final AppInstanceMapper appInstanceMapper;
    private final AppDomain appDomain;
    private final AppClientService appClientService;
    private final AppClientDomain appClientDomain;


    /**
     * 分页查询获取呀
     *
     * @param request
     * @return
     */
    @Override
    public Page<AppPageResponse> page(AppPageRequest request) {
        Page<AppPageResponse> page = new Page<>(request.getPage(), request.getSize());
        Page<AppPageResponse> result = appMapper.page(page, request);
        List<AppPageResponse> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        List<Long> appIdList = records.stream().map(AppPageResponse::getId).toList();
        List<AppClient> appClients = appClientDomain.listClients(appIdList);
        Map<Long, List<AppClient>> collect = appClients.stream().collect(Collectors.groupingBy(AppClient::getAppId));

        records.forEach(e -> {
            List<AppClient> appClientList = collect.get(e.getId());
            List<String> list = appClientList.stream().map(AppClient::getClientName).toList();
            e.setClientName(list);
        });

        return result;
    }

    @Override
    public List<App> listAll() {
        return appMapper.selectList(new LambdaQueryWrapper<App>().orderByDesc(App::getCreateTime));
    }

    /**
     * 应用详情
     */
    @Override
    public AppInfoResponse detail(Long id) {
        App byId = appDomain.findById(id);
        AppInfoResponse appInfoResponse = AppConvert.INSTANCE.toAppInfoResponse(byId);
        List<AppClient> appClients = appClientDomain.listClients(Collections.singletonList(byId.getId()));
        appInfoResponse.setClientSize(appClients.size());
        return appInfoResponse;
    }

    /**
     * 创建应用信息
     */
    @Override
    public Long create(AppRequest request) {
        appDomain.checkCodeExists(null, request.getAppCode());
        App app = AppConvert.INSTANCE.toApp(request);
        appMapper.insert(app);
        List<AppClientRequest> appClientRequests = request.getClients();
        List<AppClient> appClientList = appClientRequests.stream().map(e -> {
            AppClient appClient = AppClientConvert.INSTANCE.toAppClient(e);
            appClient.setAppId(app.getId());
            appClient.setAppCode(app.getAppCode());
            return appClient;
        }).toList();
        appClientDomain.saveClient(appClientList);
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(Long id, AppRequest request) {
        App app = appDomain.findById(id);
        appDomain.checkCodeExists(id, request.getAppCode());
        AppConvert.INSTANCE.copyApp(request, app);

        appMapper.updateById(app);
        appClientService.syncClientsForApp(app, request.getClients());
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        App app = appDomain.findById(id);
        app.setStatus(StatusEnum.NORMAL.getCode());
        appMapper.updateById(app);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        App app = appDomain.findById(id);
        Long enabledInstanceCount = appInstanceMapper.selectCount(new LambdaQueryWrapper<AppInstance>()
                .eq(AppInstance::getAppId, id)
                .eq(AppInstance::getStatus, StatusEnum.NORMAL.getCode()));
        if (enabledInstanceCount > 0) {
            throw new BizException(AppErrorCode.APP_HAS_ENABLED_INSTANCE);
        }
        app.setStatus(StatusEnum.DISABLED.getCode());
        appMapper.updateById(app);
        return Boolean.TRUE;
    }

}
