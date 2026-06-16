package com.market.goods.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.market.goods.util.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 统一捕获各类异常并封装为 Result 格式返回前端。
 * 覆盖：业务异常、参数校验异常、Sa-Token 权限异常、未知异常
 *
 * 适配 SpringBoot 4.1.0：
 *   - 使用 jakarta.validation（非 javax.validation）
 *   - NoResourceFoundException 替代已废弃的 NoHandlerFoundException
 *
 * @author goods-market
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 捕获自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 捕获 @RequestBody 参数校验异常（@Valid + @NotNull / @NotBlank 等）
     * 触发场景：POST/PUT 请求体中的字段校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 捕获 @RequestParam / @PathVariable 参数校验异常
     * 触发场景：URL 路径或查询参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 捕获表单绑定异常（@ModelAttribute 绑定失败）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("表单绑定失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 捕获缺少必需请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.fail(400, "缺少参数：" + e.getParameterName());
    }

    /**
     * 捕获请求方法不支持异常（如用 GET 访问 POST 接口）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法：{}", e.getMethod());
        return Result.fail(405, "不支持的请求方法：" + e.getMethod());
    }

    // ==================== Sa-Token 权限异常 ====================

    /**
     * 捕获未登录异常（访问需登录的接口但未携带有效 token）
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e) {
        log.warn("未登录访问：type={}", e.getType());
        String message = "NOT_TOKEN".equals(e.getType()) ? "未登录，请先登录" : "登录已过期，请重新登录";
        return Result.fail(401, message);
    }

    /**
     * 捕获无角色异常（已登录但角色不符）
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRole(NotRoleException e) {
        log.warn("角色不足：需要角色 {}", e.getRole());
        return Result.fail(403, "权限不足，需要角色：" + e.getRole());
    }

    /**
     * 捕获无权限异常（已登录但缺少指定权限）
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermission(NotPermissionException e) {
        log.warn("权限不足：需要权限 {}", e.getPermission());
        return Result.fail(403, "权限不足，需要权限：" + e.getPermission());
    }

    // ==================== 静态资源 / 404 ====================

    /**
     * 捕获 404 资源不存在
     * SpringBoot 4.x 使用 NoResourceFoundException 替代旧版 NoHandlerFoundException
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        return Result.fail(404, "请求的资源不存在");
    }

    // ==================== 兜底 — 未知异常 ====================

    /**
     * 捕获所有未被上述处理器捕获的异常（兜底）
     * 打印完整堆栈便于排查，返回通用错误信息避免泄露内部细节
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
