package com.market.goods.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 *
 * 使用方式：
 *   throw new BusinessException("商品不存在");
 *   throw new BusinessException(404, "商品不存在");
 *   throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
 *
 * @author goods-market
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /**
     * 构造 — 使用默认错误码 500
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造 — 自定义错误码
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
