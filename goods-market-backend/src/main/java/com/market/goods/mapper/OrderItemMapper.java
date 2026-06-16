package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.goods.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细表 Mapper 接口
 *
 * 说明：订单明细的关联查询在 OrderMapper XML 中通过 collection 标签一次性完成，
 *       本接口主要提供单表 CRUD 能力（BaseMapper 已满足）。
 *
 * @author goods-market
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}
