package com.authsphere.server.common.config;

import com.authsphere.server.common.annotation.ResponseHandlerIgnore;
import com.authsphere.server.common.enums.ResponseCode;
import com.authsphere.server.common.model.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @program: AuthSphere
 * @description:
 * @author: L.J.Ran
 * @create: 2026/5/28
 */
@RestControllerAdvice
public class WebResultResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public WebResultResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.hasMethodAnnotation(ResponseHandlerIgnore.class)) {
            return false;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof R)) {
            if (body instanceof String) {
                R<String> success = R.success(String.valueOf(body));
                try {
                    return this.objectMapper.writeValueAsString(success);
                } catch (JsonProcessingException exception) {
                    return this.objectMapper.createObjectNode()
                            .put("code", ResponseCode.SYSTEM_ERROR.getCode())
                            .put("message", "响应序列化失败")
                            .put("version", "v1")
                            .toString();
                }
            } else if (ObjectUtils.isEmpty(body)) {
                return R.success();
            } else {
                return R.success(body);
            }

        } else {
            return body;
        }
    }
}
