package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.dto.CreateOrderDTO;
import com.market.goods.dto.PayDTO;
import com.market.goods.service.OrderService;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.Result;
import com.market.goods.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单模块 Controller
 *
 * 权限说明（配合 SaTokenConfig）：
 *   /user/order/** 需要登录 + 角色 user/merchant/admin
 *   /pay/wx/notify 微信支付回调（公开，不做登录拦截）
 *
 * @author goods-market
 */
@Tag(name = "订单模块", description = "创建订单、支付、取消、查询")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== 用户订单接口（需登录） ====================

    /**
     * 创建订单
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：POST /api/user/order/create
     * 请求头：Authorization: Bearer {token}
     * 参数示例：
     * {
     *   "receiverName": "张三",
     *   "receiverPhone": "13800138000",
     *   "receiverAddress": "北京市朝阳区xxx",
     *   "remark": "请尽快发货",
     *   "items": [
     *     {"productId": 1001, "quantity": 2},
     *     {"productId": 1002, "quantity": 1}
     *   ]
     * }
     *
     * 事务内核心流程：
     *   1. 乐观锁扣减库存（防超卖）
     *   2. 创建订单主记录 + 明细记录
     *   3. 清理购物车
     *   任意异常整体回滚
     */
    @Operation(summary = "创建订单", description = "提交订单，乐观锁扣减库存，事务保证一致性")
    @PostMapping("/user/order/create")
    public Result<String> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        String orderNo = orderService.createOrder(userId, dto);
        return Result.ok(orderNo, "下单成功");
    }

    /**
     * 我的订单分页查询
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：GET /api/user/order/list?pageNum=1&pageSize=10&status=0
     * 请求头：Authorization: Bearer {token}
     *
     * status：0=待支付 1=已支付 2=已取消 3=已完成（不传则查全部）
     */
    @Operation(summary = "我的订单列表", description = "分页查询当前用户的订单，可按状态筛选")
    @GetMapping("/user/order/list")
    public Result<PageResult<OrderVO>> listMyOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        long userId = StpUtil.getLoginIdAsLong();
        PageResult<OrderVO> result = orderService.listMyOrders(userId, pageNum, pageSize, status);
        return Result.ok(result);
    }

    /**
     * 订单详情（含商品明细）
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：GET /api/user/order/detail/GM2026061415301200000158
     * 请求头：Authorization: Bearer {token}
     *
     * 返回 OrderVO 内含 items 列表（OrderItemVO）
     */
    @Operation(summary = "订单详情", description = "查询订单详情，含商品明细列表")
    @GetMapping("/user/order/detail/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo) {
        long userId = StpUtil.getLoginIdAsLong();
        OrderVO vo = orderService.getOrderDetail(userId, orderNo);
        return Result.ok(vo);
    }

    /**
     * 取消订单
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：PUT /api/user/order/cancel/GM2026061415301200000158
     * 请求头：Authorization: Bearer {token}
     *
     * 条件：仅待支付(0)状态可取消
     * 事务内操作：修改状态为已取消 + 回滚商品库存（乐观锁）
     */
    @Operation(summary = "取消订单", description = "待支付订单可取消，自动回滚库存")
    @PutMapping("/user/order/cancel/{orderNo}")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(userId, orderNo);
        return Result.ok(null, "订单已取消");
    }

    // ==================== 支付接口 ====================

    /**
     * 模拟支付（演示调试用）
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：POST /api/user/order/mock-pay
     * 请求头：Authorization: Bearer {token}
     * 参数：{"orderNo": "GM2026061415301200000158", "payMethod": 2}
     *
     * 直接修改订单为已支付状态，生产环境应移除此接口
     */
    @Operation(summary = "模拟支付（调试用）", description = "直接修改订单为已支付，仅用于开发调试")
    @PostMapping("/user/order/mock-pay")
    public Result<Void> mockPay(@Valid @RequestBody PayDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        orderService.mockPay(userId, dto);
        return Result.ok(null, "模拟支付成功");
    }

    /**
     * 微信支付V3：生成预支付下单参数
     *
     * 权限：需要登录（user/merchant/admin）
     * 请求：POST /api/user/order/wx-pay/GM2026061415301200000158
     * 请求头：Authorization: Bearer {token}
     *
     * 返回前端调起微信支付所需的参数（prepayId 等）
     */
    @Operation(summary = "微信支付下单", description = "生成微信支付V3预支付参数，前端调起支付")
    @PostMapping("/user/order/wx-pay/{orderNo}")
    public Result<Map<String, Object>> createWxPayOrder(@PathVariable String orderNo) {
        long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = orderService.createWxPayOrder(userId, orderNo);
        return Result.ok(result);
    }

    /**
     * 微信支付V3：异步回调接口
     *
     * 权限：公开访问（微信服务器直接调用，无需登录）
     * 请求：POST /api/pay/wx/notify
     *
     * 微信支付回调流程：
     *   1. 微信服务器 POST 请求本接口
     *   2. WxPayV3Util.parseNotify() 验签 + 解密
     *   3. 幂等校验（已支付不重复处理）
     *   4. 修改订单状态为已支付
     *   5. 返回 "SUCCESS" 告知微信不用重试
     *
     * 注意：此接口在 SaTokenConfig 中通过 excludePathPatterns 排除登录拦截
     */
    @Operation(summary = "微信支付回调", description = "微信服务器异步通知，验签+幂等处理")
    @PostMapping("/pay/wx/notify")
    public String handleWxPayNotify(HttpServletRequest request) {
        return orderService.handleWxPayNotify(request);
    }
}
