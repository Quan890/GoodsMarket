package com.market.goods.service;

import com.market.goods.dto.CreateOrderDTO;
import com.market.goods.dto.PayDTO;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 订单模块 Service 接口
 *
 * @author goods-market
 */
public interface OrderService {

    /**
     * 创建订单（核心事务方法）
     *
     * 事务内操作：
     *   1. 乐观锁扣减商品库存（防超卖）
     *   2. 新增订单主记录
     *   3. 批量新增订单明细
     *   4. 清空用户选中的购物车条目
     *   任意异常整体回滚
     *
     * @param userId 当前登录用户ID
     * @param dto    下单参数（收货信息 + 商品明细）
     * @return 订单编号
     */
    String createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 我的订单分页查询
     *
     * @param userId   当前登录用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   订单状态（可选）
     * @return 分页结果
     */
    PageResult<OrderVO> listMyOrders(Long userId, int pageNum, int pageSize, Integer status);

    /**
     * 订单详情查询
     *
     * @param userId  当前登录用户ID
     * @param orderNo 订单编号
     * @return 订单详情（含商品明细列表）
     */
    OrderVO getOrderDetail(Long userId, String orderNo);

    /**
     * 取消订单
     *
     * 条件：仅待支付状态可取消
     * 事务内操作：
     *   1. 修改订单状态为已取消
     *   2. 回滚商品库存（乐观锁）
     *
     * @param userId  当前登录用户ID
     * @param orderNo 订单编号
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 模拟支付（演示调试用，直接修改订单为已支付）
     *
     * @param userId 当前登录用户ID
     * @param dto    支付参数（订单号 + 支付方式）
     */
    void mockPay(Long userId, PayDTO dto);

    /**
     * 微信支付V3：生成预支付下单参数
     *
     * @param userId  当前登录用户ID
     * @param orderNo 订单编号
     * @return 前端调起支付所需的参数 Map
     */
    Map<String, Object> createWxPayOrder(Long userId, String orderNo);

    /**
     * 微信支付V3：异步回调处理
     *
     * 流程：验签 → 解密 → 幂等校验 → 修改订单状态
     *
     * @param request HttpServletRequest
     * @return "SUCCESS" 或 "FAIL"
     */
    String handleWxPayNotify(HttpServletRequest request);

    /**
     * 定时任务入口：超时未支付订单自动关闭并回滚库存
     *
     * 建议 @Scheduled(cron = "0 * /5 * * * ?") 每5分钟执行一次
     */
    void closeTimeoutOrders();
}
