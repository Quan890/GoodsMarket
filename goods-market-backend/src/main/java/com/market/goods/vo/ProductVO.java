package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情视图对象
 *
 * 返回场景：商品列表页、商品详情页、搜索结果
 * 包含商品基础信息 + 所属店铺名称 + 当前用户是否已收藏（需业务层填充）
 *
 * @author goods-market
 */
@Data
public class ProductVO {

    /** 商品ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 副标题 */
    private String subtitle;

    /** 主图URL */
    private String mainImage;

    /** 商品图片集（JSON 数组字符串，前端自行解析为图片列表） */
    private String images;

    /** 商品详情（富文本 HTML） */
    private String detail;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /**
     * 商品状态
     * 0=下架 1=上架
     */
    private Integer status;

    /** 排序权重 */
    private Integer sortOrder;

    // ========== 店铺信息（关联查询填充） ==========

    /** 商家ID */
    private Long merchantId;

    /** 所属店铺名称 */
    private String shopName;

    // ========== 用户维度信息（业务层按需填充） ==========

    /**
     * 当前登录用户是否已收藏该商品
     * null=未登录不返回该字段 / true=已收藏 / false=未收藏
     */
    private Boolean isCollected;

    /** 商品创建时间 */
    private LocalDateTime createTime;
}
