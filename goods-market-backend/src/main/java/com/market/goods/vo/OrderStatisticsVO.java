package com.market.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理员首页统计数据视图对象
 *
 * 返回场景：管理员后台首页 / Dashboard
 * 说明：汇总展示平台核心经营数据，帮助管理员快速了解运营状况
 *
 * @author goods-market
 */
@Data
public class OrderStatisticsVO {

    // ========== 订单统计 ==========

    /** 订单总数（历史累计） */
    private Long totalOrders;

    /** 今日新增订单数 */
    private Long todayOrders;

    /** 待支付订单数（需关注超时取消） */
    private Long pendingPaymentOrders;

    /** 待发货/已支付订单数 */
    private Long pendingDeliveryOrders;

    // ========== 金额统计 ==========

    /** 历史累计销售额（已完成订单的实付金额总和） */
    private BigDecimal totalAmount;

    /** 今日销售额（今日已完成订单的实付金额总和） */
    private BigDecimal todayAmount;

    // ========== 用户 & 商家统计 ==========

    /** 注册用户总数 */
    private Long totalUsers;

    /** 今日新增用户数 */
    private Long todayUsers;

    /** 商家总数（审核通过的） */
    private Long totalMerchants;

    /** 待审核商家数 */
    private Long pendingAuditMerchants;

    // ========== 商品统计 ==========

    /** 上架商品总数 */
    private Long totalProducts;

    /** 今日新增商品数 */
    private Long todayProducts;
}
