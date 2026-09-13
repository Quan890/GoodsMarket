package com.market.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.dto.CreateOrderDTO;
import com.market.goods.dto.PayDTO;
import com.market.goods.entity.*;
import com.market.goods.enums.OrderStatusEnum;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.*;
import com.market.goods.service.OrderService;
import com.market.goods.util.OrderNoUtil;
import com.market.goods.util.OrderItemFiller;
import com.market.goods.util.PageUtil;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.util.WxPayV3Util;
import com.market.goods.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单模块 Service 实现类
 *
 * 本类是项目核心模块，涉及：
 *   - @Transactional 事务一致性（扣库存 + 创建订单 + 清购物车 原子操作）
 *   - @Version 乐观锁防超卖（多用户同时抢购同一商品时保证库存正确性）
 *   - 跨商家拆单（购物车结算的商品可能来自多个商家，按商家拆分为多个子订单）
 *   - 订单状态机（待支付 → 已支付 → 已发货 → 已完成，任意状态可条件更新防并发）
 *   - 微信支付V3集成（预下单 + 异步回调验签 + 幂等处理）
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;
    private final MerchantMapper merchantMapper;
    private final WxPayV3Util wxPayV3Util;
    private final OrderItemFiller orderItemFiller;

    @Value("${wechat.pay.notify-url}")
    private String wxPayNotifyUrl;

    /**
     * 创建订单（核心事务方法）
     *
     * ============================================================
     * 事务边界：@Transactional 保证以下操作要么全部成功，要么全部回滚
     *   操作1: 乐观锁扣减商品库存（UPDATE ... WHERE version = ?）
     *   操作2: 按商家拆分新增订单主记录（INSERT INTO order）
     *   操作3: 批量新增订单明细（INSERT INTO order_item × N）
     *   操作4: 清空用户选中的购物车条目（逻辑删除）
     * ============================================================
     *
     * 拆单规则：一次结算的商品可能来自多个商家，每个商家生成一个独立子订单，
     *          各子订单金额只包含本商家的商品，商家只能看到自己店铺的订单。
     *
     * 并发防超卖原理：
     *   - 每个商品有 version 字段，扣减时 SQL 为：
     *     UPDATE product SET stock=stock-?, version=version+1 WHERE id=? AND version=? AND stock>=?
     *   - 并发冲突时影响行数=0，业务层抛出"库存不足，请重试"，整个事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> createOrder(Long userId, CreateOrderDTO dto) {
        List<CreateOrderDTO.OrderItemDTO> rawItems = dto.getItems();

        // ==================== 0. 合并重复商品（同一商品多条明细合并数量） ====================
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();
        for (CreateOrderDTO.OrderItemDTO item : rawItems) {
            quantityByProduct.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        // ==================== 1. 校验商品并扣减库存 ====================
        // 商品按商家分组：merchantId -> 该商家的商品明细；productMap 缓存商品快照
        Map<Long, List<CreateOrderDTO.OrderItemDTO>> itemsByMerchant = new LinkedHashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : quantityByProduct.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            // 查询商品快照（当前库存和版本号）
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在：" + productId);
            }
            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架：" + product.getName());
            }
            if (product.getStock() < quantity) {
                throw new BusinessException("商品库存不足：" + product.getName()
                        + "，库存仅剩 " + product.getStock() + " 件");
            }

            // ★ 乐观锁扣减库存 ★
            // 返回影响行数：1=成功，0=版本冲突或库存不足
            int affected = productMapper.deductStock(product.getId(), quantity, product.getVersion());
            if (affected == 0) {
                // ★ 并发冲突：其他请求已修改了该商品的 version ★
                throw new BusinessException("商品下单繁忙，请重试：" + product.getName());
            }

            // 按商家归组
            productMap.put(productId, product);
            CreateOrderDTO.OrderItemDTO merged = new CreateOrderDTO.OrderItemDTO();
            merged.setProductId(productId);
            merged.setQuantity(quantity);
            itemsByMerchant.computeIfAbsent(product.getMerchantId(), k -> new ArrayList<>()).add(merged);
        }

        // ==================== 2. 按商家拆分创建订单 ====================
        List<String> orderNos = new ArrayList<>();
        for (Map.Entry<Long, List<CreateOrderDTO.OrderItemDTO>> merchantEntry : itemsByMerchant.entrySet()) {
            Long merchantId = merchantEntry.getKey();
            List<CreateOrderDTO.OrderItemDTO> merchantItems = merchantEntry.getValue();

            // 计算本商家订单总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CreateOrderDTO.OrderItemDTO item : merchantItems) {
                Product product = productMap.get(item.getProductId());
                totalAmount = totalAmount.add(
                        product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 创建订单主记录
            String orderNo = OrderNoUtil.generate();
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);       // 暂不扣减优惠，实付=总金额
            order.setStatus(OrderStatusEnum.UNPAID.getCode());
            order.setReceiverName(dto.getReceiverName());
            order.setReceiverPhone(dto.getReceiverPhone());
            order.setReceiverAddress(dto.getReceiverAddress());
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);

            // 创建订单明细（商品信息快照）
            for (CreateOrderDTO.OrderItemDTO item : merchantItems) {
                Product product = productMap.get(item.getProductId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setOrderNo(orderNo);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());              // 商品名称快照
                orderItem.setProductImage(product.getMainImage());        // 商品图片快照
                orderItem.setUnitPrice(product.getPrice());               // 下单时单价快照
                orderItem.setQuantity(item.getQuantity());
                orderItem.setTotalPrice(
                        product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                );
                orderItemMapper.insert(orderItem);
            }

            orderNos.add(orderNo);
        }

        // ==================== 3. 清空用户选中的购物车条目 ====================
        // 删除购物车中已下单的商品（逻辑删除）
        for (Long productId : quantityByProduct.keySet()) {
            cartMapper.delete(new LambdaUpdateWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getProductId, productId));
        }

        log.info("订单创建成功：orderNos={}, userId={}, 商家数={}", orderNos, userId, orderNos.size());
        return orderNos;
    }

    /**
     * 我的订单分页查询
     */
    @Override
    public PageResult<OrderVO> listMyOrders(Long userId, int pageNum, int pageSize, Integer status) {
        Page<OrderVO> page = PageUtil.buildPage(pageNum, pageSize);
        IPage<OrderVO> result = orderMapper.selectOrderPageWithMerchant(page, userId, status);

        // 填充商品明细（列表页展示商品缩略图与件数）+ 状态描述（前端直接展示中文）
        orderItemFiller.fill(result.getRecords());
        result.getRecords().forEach(this::fillStatusDesc);
        return PageUtil.toPageResult(result);
    }

    /**
     * 订单详情查询（含商品明细列表）
     *
     * 使用 OrderMapper.selectOrderDetailByOrderNo（XML 中 collection 一对多映射）
     */
    @Override
    public OrderVO getOrderDetail(Long userId, String orderNo) {
        OrderVO vo = orderMapper.selectOrderDetailByOrderNo(orderNo);
        if (vo == null) {
            throw new BusinessException("订单不存在");
        }
        // 校验订单归属：买家可以查看自己的订单，商家可以查看自己店铺的订单
        boolean isOwner = vo.getUserId() != null && vo.getUserId().equals(userId);
        if (!isOwner) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            isOwner = merchant != null && vo.getMerchantId() != null
                    && vo.getMerchantId().equals(merchant.getId());
        }
        if (!isOwner) {
            throw new BusinessException("无权查看该订单");
        }

        fillStatusDesc(vo);
        return vo;
    }

    /**
     * 取消订单
     *
     * 条件：仅待支付(0)状态可取消
     * 事务内操作：
     *   1. 条件更新订单状态为已取消（WHERE status=0，防止与支付并发冲突）
     *   2. 回滚商品库存（乐观锁恢复）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        // 1. 查询订单并校验归属
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 条件更新：仅当订单仍处于待支付时取消（防止与支付回调并发）
        int affected = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getStatus, OrderStatusEnum.CANCELLED.getCode())
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode()));
        if (affected == 0) {
            throw new BusinessException("当前订单状态不允许取消，状态："
                    + OrderStatusEnum.ofCode(order.getStatus()).getDesc());
        }

        // 3. 回滚商品库存（乐观锁恢复）
        rollbackOrderStock(orderNo);

        log.info("订单已取消：orderNo={}, userId={}", orderNo, userId);
    }

    /**
     * 商家发货
     *
     * 条件：订单属于当前商家且状态为已支付(1)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long userId, String orderNo) {
        // 1. 校验当前用户是商家且订单属于该商家
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new BusinessException("商家信息不存在，请先完成入驻申请");
        }

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!merchant.getId().equals(order.getMerchantId())) {
            throw new BusinessException("无权操作其他店铺的订单");
        }
        if (!OrderStatusEnum.canShip(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许发货，状态："
                    + OrderStatusEnum.ofCode(order.getStatus()).getDesc());
        }

        // 2. 条件更新为已发货（WHERE status=1）
        int affected = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getStatus, OrderStatusEnum.SHIPPED.getCode())
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getStatus, OrderStatusEnum.PAID.getCode()));
        if (affected == 0) {
            throw new BusinessException("发货失败，订单状态已变化，请刷新后重试");
        }

        log.info("订单已发货：orderNo={}, merchantId={}", orderNo, merchant.getId());
    }

    /**
     * 用户确认收货
     *
     * 条件：订单属于当前用户且状态为已发货(4)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatusEnum.canReceipt(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许确认收货，状态："
                    + OrderStatusEnum.ofCode(order.getStatus()).getDesc());
        }

        int affected = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getStatus, OrderStatusEnum.COMPLETED.getCode())
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, OrderStatusEnum.SHIPPED.getCode()));
        if (affected == 0) {
            throw new BusinessException("确认收货失败，订单状态已变化，请刷新后重试");
        }

        log.info("订单已确认收货：orderNo={}, userId={}", orderNo, userId);
    }

    /**
     * 查询订单支付结果（前端支付后轮询使用）
     */
    @Override
    public Map<String, Object> getPayResult(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatusEnum statusEnum = OrderStatusEnum.ofCode(order.getStatus());
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("statusDesc", statusEnum != null ? statusEnum.getDesc() : "未知");
        boolean paid = order.getStatus() == OrderStatusEnum.PAID.getCode()
                || order.getStatus() == OrderStatusEnum.SHIPPED.getCode()
                || order.getStatus() == OrderStatusEnum.COMPLETED.getCode();
        result.put("paid", paid);
        return result;
    }

    /**
     * 模拟支付（演示调试用）
     *
     * 直接将订单状态从待支付(0)改为已支付(1)，设置支付时间和方式
     * 条件更新防并发：仅 status=0 时可支付，重复支付/已取消订单会失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(Long userId, PayDTO dto) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, dto.getOrderNo())
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatusEnum.canPay(order.getStatus())) {
            throw new BusinessException("订单不可支付，当前状态："
                    + OrderStatusEnum.ofCode(order.getStatus()).getDesc());
        }

        // 条件更新：仅待支付状态可支付（幂等，防重复回调/重复点击）
        int affected = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .set(Order::getStatus, OrderStatusEnum.PAID.getCode())
                        .set(Order::getPayTime, LocalDateTime.now())
                        .set(Order::getPayMethod, dto.getPayMethod())
                        .eq(Order::getOrderNo, dto.getOrderNo())
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode()));
        if (affected == 0) {
            throw new BusinessException("支付失败，订单状态已变化，请刷新后重试");
        }

        log.info("【模拟支付】订单已支付：orderNo={}, payMethod={}", dto.getOrderNo(), dto.getPayMethod());
    }

    /**
     * 微信支付V3：生成预支付下单参数
     *
     * 流程：
     *   1. 校验订单存在且可支付
     *   2. 委托 WxPayV3Util 调用微信支付预下单接口（封装SDK细节）
     *   3. 返回前端调起支付所需的参数
     */
    @Override
    public Map<String, Object> createWxPayOrder(Long userId, String orderNo) {
        // 1. 校验订单
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatusEnum.canPay(order.getStatus())) {
            throw new BusinessException("订单不可支付");
        }

        // 2. 委托 WxPayV3Util 创建预支付订单（所有SDK交互封装在工具类内部）
        try {
            int amountInFen = order.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue();
            Map<String, Object> payParams = wxPayV3Util.createNativePrepay(
                    orderNo, amountInFen, "好物集市-订单支付", wxPayNotifyUrl
            );
            payParams.put("orderNo", orderNo);
            payParams.put("payAmount", order.getPayAmount());
            return payParams;
        } catch (Exception e) {
            log.error("微信支付预下单失败：orderNo={}, error={}", orderNo, e.getMessage(), e);
            throw new BusinessException("支付下单失败，请稍后重试");
        }
    }

    /**
     * 微信支付V3：异步回调处理
     *
     * 流程：
     *   1. 验签 + 解密回调报文
     *   2. 幂等校验（已支付的订单不重复处理）
     *   3. 修改订单状态为已支付
     *
     * 微信支付要求回调接口返回 "SUCCESS" 表示处理成功，
     * 否则微信会在24小时内重试最多8次回调
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWxPayNotify(HttpServletRequest request) {
        try {
            // 1. 验签 + 解密（WxPayV3Util 内部调用 SDK 完成，返回关键字段 Map）
            Map<String, String> notifyData = wxPayV3Util.parseNotifyToMap(request);

            // 2. 提取关键信息
            String orderNo = notifyData.get("out_trade_no");
            String tradeState = notifyData.get("trade_state");

            log.info("微信支付回调：orderNo={}, tradeState={}", orderNo, tradeState);

            // 3. 查询订单
            Order order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getOrderNo, orderNo)
            );
            if (order == null) {
                log.warn("回调订单不存在：orderNo={}", orderNo);
                return "FAIL";
            }

            // 4. 幂等校验：已支付的订单不重复处理（防止微信重复回调）
            if (OrderStatusEnum.PAID.getCode() == order.getStatus()) {
                log.info("订单已支付，跳过重复回调：orderNo={}", orderNo);
                return "SUCCESS";    // 返回 SUCCESS 告知微信不用重试
            }

            // 5. 校验支付金额（防止篡改攻击）
            String totalStr = notifyData.get("total");
            if (totalStr != null) {
                int wxAmountFen = Integer.parseInt(totalStr);
                int orderAmountFen = order.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue();
                if (wxAmountFen != orderAmountFen) {
                    log.error("微信支付金额与订单金额不匹配！orderNo={}, 微信支付={}分, 订单金额={}分",
                            orderNo, wxAmountFen, orderAmountFen);
                    return "FAIL";
                }
            }

            // 6. 条件更新为已支付（仅 SUCCESS 且仍处于待支付状态）
            if (!"SUCCESS".equals(tradeState)) {
                log.warn("微信回调交易状态非SUCCESS，不更新订单：orderNo={}, tradeState={}", orderNo, tradeState);
                return "SUCCESS";
            }
            int affected = orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .set(Order::getStatus, OrderStatusEnum.PAID.getCode())
                            .set(Order::getPayTime, LocalDateTime.now())
                            .set(Order::getPayMethod, 2)     // 微信支付
                            .eq(Order::getOrderNo, orderNo)
                            .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode()));
            log.info("微信支付回调处理{}：orderNo={}", affected > 0 ? "成功" : "重复跳过", orderNo);
            return "SUCCESS";

        } catch (Exception e) {
            log.error("微信支付回调处理异常：{}", e.getMessage(), e);
            return "FAIL";
        }
    }

    /**
     * 定时任务：超时未支付订单自动关闭并回滚库存
     *
     * 执行逻辑：
     *   1. 查询所有待支付且创建超过30分钟的订单
     *   2. 条件更新（WHERE status=0）为已取消，防止与用户支付并发冲突
     *   3. 更新成功的订单回滚商品库存（乐观锁）
     */
    @Override
    public void closeTimeoutOrders() {
        // 查询超时30分钟未支付的订单
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<Order> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode())
                        .lt(Order::getCreateTime, timeout)
        );

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("定时任务：发现 {} 个超时未支付订单，开始自动关闭", timeoutOrders.size());

        for (Order order : timeoutOrders) {
            try {
                // 条件更新：仅当订单仍是待支付时关闭（用户可能恰好在此时完成支付）
                int affected = orderMapper.update(null,
                        new LambdaUpdateWrapper<Order>()
                                .set(Order::getStatus, OrderStatusEnum.CANCELLED.getCode())
                                .eq(Order::getId, order.getId())
                                .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode()));
                if (affected == 0) {
                    continue;   // 状态已变化（如用户刚支付），跳过
                }

                // 回滚商品库存
                rollbackOrderStock(order.getOrderNo());
                log.info("定时任务：已关闭超时订单 orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("定时任务：关闭订单失败 orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 回滚订单中所有商品的库存（乐观锁恢复）
     */
    private void rollbackOrderStock(String orderNo) {
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderNo, orderNo)
        );
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                // ★ 乐观锁恢复库存 ★
                int affected = productMapper.restoreStock(
                        product.getId(),
                        item.getQuantity(),
                        product.getVersion()
                );
                if (affected == 0) {
                    log.warn("库存回滚失败（并发冲突），需人工处理：productId={}, quantity={}",
                            item.getProductId(), item.getQuantity());
                }
            }
        }
    }

    /**
     * 填充订单状态描述和支付方式描述
     */
    private void fillStatusDesc(OrderVO vo) {
        OrderStatusEnum statusEnum = OrderStatusEnum.ofCode(vo.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");

        if (vo.getPayMethod() != null) {
            vo.setPayMethodDesc(vo.getPayMethod() == 1 ? "支付宝" : "微信");
        }
    }
}
