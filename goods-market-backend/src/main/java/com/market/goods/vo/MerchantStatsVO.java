package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家经营统计 VO
 *
 * 接口：GET /merchant/stats
 * 商家中心首页数据看板
 *
 * @author goods-market
 */
@Data
public class MerchantStatsVO {

    // ========== 商品统计 ==========

    /** 商品总数 */
    private Long totalProducts;

    /** 在售商品数（status=1） */
    private Long onSaleProducts;

    // ========== 订单统计 ==========

    /** 订单总数 */
    private Long totalOrders;

    /** 今日新增订单数 */
    private Long todayOrders;

    /** 待发货订单数（已支付 status=1） */
    private Long pendingShipOrders;

    /** 待收货订单数（已发货 status=4） */
    private Long shippedOrders;

    /** 已完成订单数（status=3） */
    private Long completedOrders;

    // ========== 金额统计 ==========

    /** 累计销售额（已支付+已发货+已完成订单的实付金额之和） */
    private BigDecimal totalSales;

    /** 今日销售额 */
    private BigDecimal todaySales;
}
