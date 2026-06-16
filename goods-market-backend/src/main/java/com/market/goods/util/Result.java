package com.market.goods.util;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * 响应格式：
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... }
 * }
 *
 * 使用方式：
 *   return Result.ok();                         // 无数据成功
 *   return Result.ok(userVO);                   // 带数据成功
 *   return Result.fail("商品不存在");             // 失败
 *   return Result.fail(404, "商品不存在");        // 自定义错误码
 *
 * @param <T> 数据类型
 * @author goods-market
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200成功，其他为失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ==================== 私有构造 ====================

    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 静态工厂 — 成功 ====================

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功（带数据）
     *
     * @param data 响应数据
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功（带数据 + 自定义消息）
     *
     * @param data    响应数据
     * @param message 提示信息
     */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, message, data);
    }

    // ==================== 静态工厂 — 失败 ====================

    /**
     * 失败（默认错误码 500）
     *
     * @param message 错误信息
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败（自定义错误码）
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    // ==================== 便捷判断 ====================

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
}
