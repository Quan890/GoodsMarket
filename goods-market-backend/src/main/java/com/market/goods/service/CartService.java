package com.market.goods.service;

import com.market.goods.dto.CartOperateDTO;
import com.market.goods.util.PageUtil.PageResult;
import com.market.goods.vo.CartVO;

/**
 * 购物车模块 Service 接口
 *
 * @author goods-market
 */
public interface CartService {

    /**
     * 新增商品到购物车
     *
     * 逻辑：如果购物车中已存在该商品，则累加数量；否则新增一条记录
     *
     * @param userId 当前登录用户ID
     * @param dto    包含商品ID和添加数量
     */
    void addToCart(Long userId, CartOperateDTO dto);

    /**
     * 修改购物车商品数量
     *
     * @param userId 当前登录用户ID
     * @param dto    包含商品ID和新数量（设为0则删除该商品）
     */
    void updateQuantity(Long userId, CartOperateDTO dto);

    /**
     * 删除购物车单项
     *
     * @param userId    当前登录用户ID
     * @param productId 商品ID
     */
    void deleteCartItem(Long userId, Long productId);

    /**
     * 查询个人购物车列表（关联商品最新信息）
     *
     * @param userId   当前登录用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 购物车列表（CartVO 包含商品名称、图片、价格、库存、小计等）
     */
    PageResult<CartVO> listMyCart(Long userId, int pageNum, int pageSize);
}
