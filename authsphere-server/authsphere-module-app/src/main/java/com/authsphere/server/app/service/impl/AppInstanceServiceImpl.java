package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppInstancePageRequest;
import com.authsphere.server.app.dto.AppInstanceRequest;
import com.authsphere.server.app.enums.AppOpenMode;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.authsphere.server.app.model.AppInstance;
import com.authsphere.server.app.service.AppInstanceService;
import com.authsphere.server.app.mapper.AppInstanceMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.subject.domain.SubjectDomain;
import com.authsphere.server.subject.domain.SubjectTypeDomain;
import com.authsphere.server.subject.model.Subject;
import com.authsphere.server.subject.model.SubjectType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
* @author longjiangran
* @description 针对表【app_instance(应用实例表)】的数据库操作Service实现
* @createDate 2026-06-03 11:07:48
*/
@Service
@RequiredArgsConstructor
public class AppInstanceServiceImpl extends ServiceImpl<AppInstanceMapper, AppInstance> implements AppInstanceService {

    private final AppInstanceMapper appInstanceMapper;
    private final AppMapper appMapper;
    private final SubjectDomain subjectDomain;
    private final SubjectTypeDomain subjectTypeDomain;

    @Override
    public Page<AppInstance> page(AppInstancePageRequest request) {
        Page<AppInstance> page = new Page<>(request.getPage(), request.getSize());
        return appInstanceMapper.page(page, request);
    }

    @Override
    public List<AppInstance> listBySubject(Long subjectId) {
        return appInstanceMapper.selectList(new LambdaQueryWrapper<AppInstance>()
                .eq(AppInstance::getOwnerSubjectId, subjectId)
                .orderByDesc(AppInstance::getCreateTime));
    }

    @Override
    public AppInstance detail(Long id) {
        return findById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(AppInstanceRequest request) {
        return createInternal(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createForSubject(Long subjectId, AppInstanceRequest request) {
        request.setOwnerSubjectId(subjectId);
        return createInternal(request);
    }

    @Override
    public Boolean edit(Long id, AppInstanceRequest request) {
        AppInstance instance = findById(id);
        validateInstanceRequest(id, request);
        App app = findEnabledApp(request.getAppId());
        BeanUtils.copyProperties(request, instance, "id", "createTime", "updateTime");
        instance.setAppCode(app.getAppCode());
        instance.setRootSubjectId(resolveRootSubjectId(request));
        appInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppInstance instance = findById(id);
        findEnabledApp(instance.getAppId());
        instance.setStatus(StatusEnum.NORMAL.getCode());
        appInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppInstance instance = findById(id);
        instance.setStatus(StatusEnum.DISABLED.getCode());
        appInstanceMapper.updateById(instance);
        return Boolean.TRUE;
    }

    private Boolean createInternal(AppInstanceRequest request) {
        validateInstanceRequest(null, request);
        App app = findEnabledApp(request.getAppId());
        AppInstance instance = new AppInstance();
        BeanUtils.copyProperties(request, instance);
        instance.setAppCode(app.getAppCode());
        instance.setRootSubjectId(resolveRootSubjectId(request));
        appInstanceMapper.insert(instance);
        return Boolean.TRUE;
    }

    private void validateInstanceRequest(Long currentId, AppInstanceRequest request) {
        if (!AppOpenMode.valid(request.getOpenMode())) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DATA_ERROR);
        }
        Subject subject = subjectDomain.findById(request.getOwnerSubjectId());
        if (!Objects.equals(subject.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_SUBJECT_DATA_ERROR);
        }
        SubjectType subjectType = subjectTypeDomain.findById(subject.getSubjectTypeId());
        if (!Boolean.TRUE.equals(subjectType.getCanOpenApp())) {
            throw new BizException(AppErrorCode.APP_SUBJECT_DATA_ERROR);
        }
        App app = findEnabledApp(request.getAppId());
        Long codeCount = appInstanceMapper.selectCount(new LambdaQueryWrapper<AppInstance>()
                .eq(AppInstance::getInstanceCode, request.getInstanceCode())
                .ne(currentId != null, AppInstance::getId, currentId));
        if (codeCount > 0) {
            throw new BizException(AppErrorCode.APP_INSTANCE_CODE_EXISTS);
        }
        Long subjectAppCount = appInstanceMapper.selectCount(new LambdaQueryWrapper<AppInstance>()
                .eq(AppInstance::getOwnerSubjectId, request.getOwnerSubjectId())
                .eq(AppInstance::getAppCode, app.getAppCode())
                .ne(currentId != null, AppInstance::getId, currentId));
        if (subjectAppCount > 0) {
            throw new BizException(AppErrorCode.APP_INSTANCE_SUBJECT_APP_EXISTS);
        }
        if (request.getRootSubjectId() != null) {
            subjectDomain.findById(request.getRootSubjectId());
        }
    }

    private Long resolveRootSubjectId(AppInstanceRequest request) {
        if (request.getRootSubjectId() != null) {
            return request.getRootSubjectId();
        }
        Subject subject = subjectDomain.findById(request.getOwnerSubjectId());
        return subject.getRootSubjectId() != null ? subject.getRootSubjectId() : subject.getId();
    }

    /**
     * 应用实例只能基于启用中的应用创建或恢复启用。
     */
    private App findEnabledApp(Long appId) {
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        if (!Objects.equals(app.getStatus(), StatusEnum.NORMAL.getCode())) {
            throw new BizException(AppErrorCode.APP_DISABLED_DENIED);
        }
        return app;
    }

    private AppInstance findById(Long id) {
        AppInstance instance = appInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BizException(AppErrorCode.APP_INSTANCE_DATA_ERROR);
        }
        return instance;
    }

}

