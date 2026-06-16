package com.market.goods.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车新增/修改数量入参
 *
 * 接口：
 *   POST   /user/cart/add      — 新增商品到购物车（quantity=添加数量）
 *   PUT    /user/cart/quantity  — 修改购物车商品数量（quantity=新数量）
 *
 * @author goods-market
 */
@Data
public class CartOperateDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 数量
     * 新增时：表示添加的数量（最小1）
     * 修改时：表示最终数量（设为0则删除该商品）
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity;
}
