package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表视图对象
 *
 * 返回场景：用户收藏列表页
 * 说明：展示用户收藏的商品摘要信息 + 收藏时间，按收藏时间倒序排列
 *
 * @author goods-market
 */
@Data
public class CollectVO {

    /** 收藏记录ID（用于取消收藏操作） */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productImage;

    /** 商品副标题 */
    private String subtitle;

    /** 商品售价 */
    private BigDecimal price;

    /** 商品原价（划线价） */
    private BigDecimal originalPrice;

    /**
     * 商品状态
     * 0=下架 1=上架
     * 下架商品在收藏列表中提示"已下架"
     */
    private Integer productStatus;

    /** 收藏时间 */
    private LocalDateTime collectTime;
}
