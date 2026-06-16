package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Cart;
import com.market.goods.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 分页查询用户购物车（关联商品表获取最新商品信息）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 购物车列表（CartVO 包含商品名称、图片、价格、库存等）
     */
    IPage<CartVO> selectCartPageWithProduct(Page<CartVO> page, @Param("userId") Long userId);

    /**
     * 查询已逻辑删除的购物车记录（忽略逻辑删除过滤器）
     * 用于恢复购物车时查找 deleted=1 的记录
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 已删除的购物车记录（可能为null）
     */
    Cart selectDeletedItem(@Param("userId") Long userId, @Param("productId") Long productId);
}
