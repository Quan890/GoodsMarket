package com.market.goods.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.entity.OrderItem;
import com.market.goods.mapper.OrderItemMapper;
import com.market.goods.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单商品明细批量填充组件
 *
 * 订单列表页的 SQL（selectOrderPageWithMerchant / selectOrderPageByAdmin）
 * 只查订单主表，不返回明细。列表页需要展示商品缩略图和件数，
 * 由本组件对本页订单做一次 IN 批量查询后按 orderId 分组写入 VO，避免 N+1 查询。
 *
 * @author goods-market
 */
@Component
@RequiredArgsConstructor
public class OrderItemFiller {

    private final OrderItemMapper orderItemMapper;

    /**
     * 为订单列表批量填充 items 明细
     *
     * @param orders 当前页订单列表（就地填充）
     */
    public void fill(List<OrderVO> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> orderIds = orders.stream().map(OrderVO::getId).toList();
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        if (items.isEmpty()) {
            return;
        }
        Map<Long, List<OrderVO.OrderItemVO>> itemsByOrder = new HashMap<>();
        for (OrderItem item : items) {
            OrderVO.OrderItemVO vo = new OrderVO.OrderItemVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setUnitPrice(item.getUnitPrice());
            vo.setQuantity(item.getQuantity());
            vo.setTotalPrice(item.getTotalPrice());
            itemsByOrder.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(vo);
        }
        for (OrderVO order : orders) {
            order.setItems(itemsByOrder.get(order.getId()));
        }
    }
}
