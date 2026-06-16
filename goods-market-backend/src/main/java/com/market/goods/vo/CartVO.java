package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车视图对象（带商品信息）
 *
 * 返回场景：购物车列表页
 * 说明：购物车列表需展示商品最新信息（名称、图片、价格、库存），
 *       因此需要关联 product 表查询，而非仅返回 cart 表字段
 *
 * @author goods-market
 */
@Data
public class CartVO {

    /** 购物车记录ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称（来自 product 表实时查询） */
    private String productName;

    /** 商品主图（来自 product 表实时查询） */
    private String productImage;

    /** 商品当前售价（来自 product 表实时查询） */
    private BigDecimal price;

    /** 原价（用于划线价展示） */
    private BigDecimal originalPrice;

    /** 购物车中的购买数量 */
    private Integer quantity;

    /**
     * 是否选中（结算勾选状态）
     * 0=未选中 1=已选中
     */
    private Integer checked;

    /** 商品当前库存（用于判断是否可继续加购） */
    private Integer stock;

    /**
     * 商品状态（0=下架 1=上架）
     * 下架商品在购物车中置灰展示，不可结算
     */
    private Integer productStatus;

    /** 小计金额 = price × quantity */
    private BigDecimal subtotal;
}
