package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppPageRequest;
import com.authsphere.server.app.dto.AppRequest;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.App;
import com.authsphere.server.app.service.AppService;
import com.authsphere.server.app.mapper.AppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

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

    @Override
    public Page<App> page(AppPageRequest request) {
        Page<App> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<App>()
                .like(request.getAppCode() != null, App::getAppCode, request.getAppCode())
                .like(request.getAppName() != null, App::getAppName, request.getAppName())
                .eq(request.getAppType() != null, App::getAppType, request.getAppType())
                .eq(request.getStatus() != null, App::getStatus, request.getStatus())
                .orderByDesc(App::getCreateTime);
        return appMapper.selectPage(page, wrapper);
    }

    @Override
    public List<App> listAll() {
        return appMapper.selectList(new LambdaQueryWrapper<App>().orderByDesc(App::getCreateTime));
    }

    @Override
    public App detail(Long id) {
        return findById(id);
    }

    @Override
    public Boolean create(AppRequest request) {
        checkCodeExists(null, request.getAppCode());
        App app = new App();
        BeanUtils.copyProperties(request, app);
        app.setBuiltIn(defaultZero(request.getBuiltIn()));
        appMapper.insert(app);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppRequest request) {
        App app = findById(id);
        checkCodeExists(id, request.getAppCode());
        BeanUtils.copyProperties(request, app, "id", "createTime", "updateTime");
        app.setBuiltIn(defaultZero(request.getBuiltIn()));
        appMapper.updateById(app);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        App app = findById(id);
        app.setStatus(StatusEnum.NORMAL.getCode());
        appMapper.updateById(app);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        App app = findById(id);
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

    private App findById(Long id) {
        App app = appMapper.selectById(id);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        return app;
    }

    private void checkCodeExists(Long currentId, String appCode) {
        Long count = appMapper.selectCount(new LambdaQueryWrapper<App>()
                .eq(App::getAppCode, appCode)
                .ne(currentId != null, App::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_CODE_EXISTS);
        }
    }

    private Integer defaultZero(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

}

