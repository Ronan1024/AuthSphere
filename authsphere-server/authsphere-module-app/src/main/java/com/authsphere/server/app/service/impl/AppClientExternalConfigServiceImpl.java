package com.authsphere.server.app.service.impl;

import com.authsphere.server.app.dto.AppClientExternalConfigRequest;
import com.authsphere.server.app.error.AppErrorCode;
import com.authsphere.server.app.mapper.AppClientExternalConfigMapper;
import com.authsphere.server.app.mapper.AppClientMapper;
import com.authsphere.server.app.model.AppClient;
import com.authsphere.server.app.model.AppClientExternalConfig;
import com.authsphere.server.app.service.AppClientExternalConfigService;
import com.authsphere.server.common.enums.StatusEnum;
import com.authsphere.server.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 客户端外部平台配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class AppClientExternalConfigServiceImpl
        extends ServiceImpl<AppClientExternalConfigMapper, AppClientExternalConfig>
        implements AppClientExternalConfigService {

    private static final String MASK = "******";

    private final AppClientExternalConfigMapper configMapper;
    private final AppClientMapper appClientMapper;

    @Override
    public List<AppClientExternalConfig> listByClient(Long appClientId) {
        ensureClient(appClientId);
        return configMapper.selectList(new LambdaQueryWrapper<AppClientExternalConfig>()
                        .eq(AppClientExternalConfig::getAppClientId, appClientId)
                        .orderByDesc(AppClientExternalConfig::getCreateTime))
                .stream()
                .map(this::maskSensitive)
                .toList();
    }

    @Override
    public Boolean create(Long appClientId, AppClientExternalConfigRequest request) {
        ensureClient(appClientId);
        checkExists(null, appClientId, request);
        AppClientExternalConfig config = new AppClientExternalConfig();
        BeanUtils.copyProperties(request, config);
        config.setAppClientId(appClientId);
        protectSensitive(config);
        configMapper.insert(config);
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(Long id, AppClientExternalConfigRequest request) {
        AppClientExternalConfig config = findById(id);
        checkExists(id, config.getAppClientId(), request);
        BeanUtils.copyProperties(request, config, "id", "appClientId", "createTime", "updateTime");
        protectSensitive(config);
        configMapper.updateById(config);
        return Boolean.TRUE;
    }

    @Override
    public Boolean enable(Long id) {
        AppClientExternalConfig config = findById(id);
        config.setStatus(StatusEnum.NORMAL.getCode());
        configMapper.updateById(config);
        return Boolean.TRUE;
    }

    @Override
    public Boolean disable(Long id) {
        AppClientExternalConfig config = findById(id);
        config.setStatus(StatusEnum.DISABLED.getCode());
        configMapper.updateById(config);
        return Boolean.TRUE;
    }

    @Override
    public Boolean verify(Long id) {
        findById(id);
        return Boolean.TRUE;
    }

    private void checkExists(Long currentId, Long appClientId, AppClientExternalConfigRequest request) {
        Long count = configMapper.selectCount(new LambdaQueryWrapper<AppClientExternalConfig>()
                .eq(AppClientExternalConfig::getAppClientId, appClientId)
                .eq(AppClientExternalConfig::getProviderType, request.getProviderType())
                .eq(AppClientExternalConfig::getProviderCode, request.getProviderCode())
                .ne(currentId != null, AppClientExternalConfig::getId, currentId));
        if (count > 0) {
            throw new BizException(AppErrorCode.APP_CLIENT_EXTERNAL_CONFIG_EXISTS);
        }
    }

    private AppClientExternalConfig findById(Long id) {
        AppClientExternalConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_EXTERNAL_CONFIG_DATA_ERROR);
        }
        return config;
    }

    private void ensureClient(Long appClientId) {
        AppClient client = appClientMapper.selectById(appClientId);
        if (client == null) {
            throw new BizException(AppErrorCode.APP_CLIENT_DATA_ERROR);
        }
    }

    private void protectSensitive(AppClientExternalConfig config) {
        config.setAppSecret(protect(config.getAppSecret()));
        config.setPrivateKey(protect(config.getPrivateKey()));
    }

    private String protect(String value) {
        if (value == null || value.isBlank() || value.startsWith("ENC:") || MASK.equals(value)) {
            return value;
        }
        // 这里先保留统一保护入口，后续接入 KMS/AES 后只需要替换本方法实现。
        return "ENC:" + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private AppClientExternalConfig maskSensitive(AppClientExternalConfig source) {
        AppClientExternalConfig target = new AppClientExternalConfig();
        BeanUtils.copyProperties(source, target);
        // 外部配置列表和详情永远不返回敏感字段明文。
        if (target.getAppSecret() != null) {
            target.setAppSecret(MASK);
        }
        if (target.getPrivateKey() != null) {
            target.setPrivateKey(MASK);
        }
        return target;
    }
}
