package com.authsphere.server.app.domain;

import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppMapper;
import com.authsphere.server.app.model.App;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/6/4
 */
@Service
@RequiredArgsConstructor
public class AppDomain {
    private final AppMapper appMapper;


    /**
     * 校验判断应用 code 是否存在
     */
    public void checkCodeExists(Long currentId, String appCode) {
        Long count = appMapper.selectCount(new LambdaQueryWrapper<App>()
                .eq(App::getAppCode, appCode)
                .ne(!ObjectUtils.isEmpty(appCode), App::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_CODE_EXISTS);
        }
    }


    /**
     * 获取应用信息
     */
    public App findById(Long id) {
        App app = appMapper.selectById(id);
        if (app == null) {
            throw new BizException(AppErrorCode.APP_DATA_ERROR);
        }
        return app;
    }
}
