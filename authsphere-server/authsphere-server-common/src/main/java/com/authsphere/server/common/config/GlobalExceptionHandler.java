package com.authsphere.server.common.config;

import com.authsphere.server.common.enums.ResponseCode;
import com.authsphere.server.common.exception.BizException;
import com.authsphere.server.common.model.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一转换接口异常响应。
 *
 * @author L.J.Ran
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException exception) {
        log.error("业务异常，code={}，message={}", exception.getCode(), exception.getMessage(), exception);
        return R.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = getFieldErrorMessage(exception);
        log.error("请求参数校验失败，message={}", message, exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException exception) {
        String message = getFieldErrorMessage(exception);
        log.error("请求参数绑定失败，message={}", message, exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.error("请求参数约束校验失败，message={}", message, exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        log.error("缺少请求参数，parameter={}", exception.getParameterName(), exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), exception.getParameterName() + "不能为空");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.error("请求参数类型错误，parameter={}", exception.getName(), exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), exception.getName() + "参数类型错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.error("请求体格式错误", exception);
        return R.fail(ResponseCode.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("请求方法不支持，method={}", exception.getMethod(), exception);
        return R.fail(ResponseCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public R<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        log.error("请求媒体类型不支持，contentType={}", exception.getContentType(), exception);
        return R.fail(ResponseCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return R.fail(ResponseCode.SYSTEM_ERROR);
    }

    private String getFieldErrorMessage(BindException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
