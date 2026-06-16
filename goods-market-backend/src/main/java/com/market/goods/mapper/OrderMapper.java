package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Order;
import com.market.goods.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询用户订单列表（关联商家店铺名）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @param status 订单状态（可选，null=查全部）
     * @return 订单列表（OrderVO 包含 shopName）
     */
    IPage<OrderVO> selectOrderPageWithMerchant(Page<OrderVO> page,
                                               @Param("userId") Long userId,
                                               @Param("status") Integer status);

    /**
     * 根据订单编号查询订单详情（关联商家 + 订单明细）
     *
     * @param orderNo 订单编号
     * @return 订单详情（OrderVO 包含 items 列表）
     */
    OrderVO selectOrderDetailByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 管理员多条件分页查询订单
     *
     * @param page   分页参数
     * @param params 查询条件：orderNo（订单号模糊）、status（状态）、merchantId（商家ID）
     * @return 分页结果
     */
    IPage<OrderVO> selectOrderPageByAdmin(Page<OrderVO> page, @Param("params") Map<String, Object> params);

    /**
     * 查询已完成订单的实付金额总和（SQL聚合，避免加载全部数据到内存）
     *
     * @param wrapper 查询条件（status=已完成 + 可选时间范围）
     * @return 金额总和
     */
    BigDecimal selectSumPayAmount(@Param("ew") com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper);
}
